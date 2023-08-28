{
  description = "Compound";

  inputs = {
    devenv.url = "github:cachix/devenv";
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:NixOS/nixpkgs";
  };

  outputs = { self, flake-utils, nixpkgs, devenv }@inputs:
    flake-utils.lib.eachDefaultSystem (system:
      let
        overlays = [
          (self: super: rec {
            nodejs = super.nodejs-18_x;
            pnpm = super.nodePackages.pnpm;
            yarn = (super.yarn.override { inherit nodejs; });
          })
        ];
        pkgs = import nixpkgs { inherit overlays system; };
      in {
        devShells.default = devenv.lib.mkShell {
          inherit inputs pkgs;
          modules = [
            ({ pkgs, ... }: {
              env.OVERMIND_PORT = "3000";

              packages = with pkgs; [
                babashka
                bash
                clj-kondo
                clojure
                clojure-lsp
                git
                htmlq
                node2nix
                nodejs
                pnpm
              ];

              processes = {
                nrepl.exec = "clojure -M:dev:test -m compound.dev";
                tailwind.exec = "pnpm watch";
              };

              process.implementation = "overmind";
            })
          ];
        };
      });
}
