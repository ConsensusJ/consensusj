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
          jdk = pkgs.jdk25;
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
            gradleBuildTask = "consensusj-jrpc:nativeCompile";

            gradleFlags = [ "--info --stacktrace" ];

            # will run the gradleCheckTask (defaults to "test")
            doCheck = false;

            installPhase = ''
              mkdir -p $out/bin
              cp consensusj-jrpc/build/${mainProgram} $out/bin/${mainProgram}
              wrapProgram $out/bin/${mainProgram}
            '';
          });
        in
          self2;
    });
  };
}
