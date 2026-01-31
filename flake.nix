{
  description = "ConsensusJ devShell and consensusj-tools (jrpc for now) package";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/release-25.11";
  };

  outputs = { self, nixpkgs }:
  let
      systems = [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" "x86_64-darwin" ];
      forEachSystem = f: builtins.listToAttrs (map (system: {
        name = system;
        value = f system;
      }) systems);
  in {
    devShells = forEachSystem(system:
      let
        pkgs = import nixpkgs { inherit system; };

        jdk = pkgs.jdk25;
        graalvm = pkgs.graalvmPackages.graalvm-ce;
        # Override bitcoind to include Berkeley DB support
        # This is currently broken on macOS/Darwin so regTest via devshell only works on Linux
        bitcoind = pkgs.bitcoind.override { withWallet = true; };
      in {
        default = pkgs.mkShell {
          buildInputs = with pkgs ; [
            zlib
            jdk
            (gradle_9.override {    # Gradle Nix package uses an internally-linked JDK
                java = jdk;         # Run Gradle with this JDK
            })
            jreleaser-cli           # (Potentially) for use in publishing builds
            bitcoind                # For running tests that talk to bitcoind
            websocat                # For manually testing websocket servers
          ];
          shellHook = ''
            # setup GRAALVM_HOME
            export GRAALVM_HOME=${graalvm}
            echo "Welcome to ConsensusJ"
            bitcoind --version | head -n1
          '';
        };
      }
    );
    packages = forEachSystem (system: {
      consensusj-tools =
        let
          pkgs = import nixpkgs {
            inherit system;
          };
          mainProgram = "jrpc";
          echodProgram = "jrpc-echod";
          walletdProgram = "walletd";
          jdk = pkgs.jdk25_headless;
          graalvm = pkgs.graalvmPackages.graalvm-ce;
          gradle = pkgs.gradle_9.override {
            java = jdk;  # Run Gradle with this JDK
          };
          self2 = pkgs.stdenv.mkDerivation (_finalAttrs: {
            pname = "consensusj-tools";
            version = "0.7.0-SNAPSHOT";
            meta = {
              inherit mainProgram;
            };

            src = self;  # project root is source

            nativeBuildInputs = [gradle pkgs.makeWrapper graalvm];

            mitmCache = gradle.fetchDeps {
              pkg = self2;
              # update or regenerate this by running:
              #  $(nix build .#consensusj-tools.mitmCache.updateScript --print-out-paths)
              data = ./nix-deps.json;
            };

            preBuild = ''
              export GRAALVM_HOME=${graalvm}
            '';
            gradleBuildTask = "consensusj-jrpc:nativeCompile consensusj-jrpc-echod:installDist cj-btc-walletd:installDist";

            gradleFlags = [ "--info --stacktrace" ];

            # will run the gradleCheckTask (defaults to "test")
            doCheck = false;

            installPhase = ''
              mkdir -p $out/bin
              cp consensusj-jrpc/build/${mainProgram} $out/bin/${mainProgram}
              wrapProgram $out/bin/${mainProgram}

              # Package `jrpc-echod` as JARs and a JDK
              mkdir -p $out/{bin,share/${echodProgram}/lib}
              cp consensusj-jrpc-echod/build/install/${echodProgram}/lib/*.jar $out/share/${echodProgram}/lib
              # Compute CLASSPATH: all .jar files in $out/share/${echodProgram}/lib
              export ECHOD_PATH=$(find $out/share/${echodProgram}/lib -name "*.jar" -printf ':%p' | sed 's|^:||')  # Colon-separated, no leading :
              makeWrapper ${jdk}/bin/java $out/bin/${echodProgram} \
                    --add-flags "-cp $ECHOD_PATH org.consensusj.jsonrpc.daemon.Application"

              # Package `walletd` as JARs and a JDK
              mkdir -p $out/{bin,share/${walletdProgram}/lib}
              cp cj-btc-walletd/build/install/${walletdProgram}/lib/*.jar $out/share/${walletdProgram}/lib
              # Compute CLASSPATH: all .jar files in $out/share/${walletdProgram}/lib
              export WALLETD_PATH=$(find $out/share/${walletdProgram}/lib -name "*.jar" -printf ':%p' | sed 's|^:||')  # Colon-separated, no leading :
              makeWrapper ${jdk}/bin/java $out/bin/${walletdProgram} \
                    --add-flags "-cp $WALLETD_PATH org.consensusj.daemon.micronaut.Application"
            '';
          });
        in
          self2;
    });
  };
}
