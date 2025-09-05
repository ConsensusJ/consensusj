{
  # This currently just adds a `bitcoind` for regTest testing
  description = "in-progress devshell support for ConsensusJ";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        # Override bitcoind to include Berkeley DB support
        # This is currently broken on macOS/Darwin so regTest via devshell only works on Linux
        bitcoind = pkgs.bitcoind.override { withWallet = true; };
      in {
        packages.bitcoind = bitcoind;

        devShells.default = pkgs.mkShell {
          buildInputs = [
            pkgs.jdk  # We're still building with ./gradlew, so install JDK not gradle
            bitcoind
          ];
          shellHook = ''
            echo "Welcome to ConsensusJ"
            echo "  $(which bitcoind)"
            bitcoind --version | head -n1
          '';
        };
      });
}
