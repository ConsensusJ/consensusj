{
  # This currently just adds a `bitcoind` for regTest testing
  description = "in-progress devshell support for ConsensusJ";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
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

        graalvm = pkgs.graalvmPackages.graalvm-ce;
        # Override bitcoind to include Berkeley DB support
        # This is currently broken on macOS/Darwin so regTest via devshell only works on Linux
        bitcoind = pkgs.bitcoind.override { withWallet = true; };
      in {
        default = pkgs.mkShell {
          buildInputs = with pkgs ; [
            graalvm
            (gradle_8.override {    # Gradle Nix package uses an internally-linked JDK
                java = graalvm;        # Run Gradle with this JDK
            })
            bitcoind
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
  };
}
