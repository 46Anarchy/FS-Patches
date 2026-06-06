{
    description = "WaifuHax Flake";

    inputs = {
        nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    };

    outputs = { self, nixpkgs, ... }:
    let
        allSystems = [
            "x86_64-linux" # 64-bit Intel/AMD Linux
            "aarch64-linux" # 64-bit ARM Linux
            "x86_64-darwin" # 64-bit Intel macOS
            "aarch64-darwin" # 64-bit ARM macOS
        ];
        forAllSystems = f: nixpkgs.lib.genAttrs allSystems (system: f {
            pkgs = import nixpkgs { inherit system; };
        });
    in
    {
        devShells = forAllSystems({ pkgs }:
            let libs = with pkgs; [
                jdk8_headless
                jdt-language-server
                python3
            ];
            in
            {
                default = pkgs.mkShell {
                    env = {
                        JAVA_HOME = "${pkgs.jdk8_headless}";
                        JDTLS_HOME = "${pkgs.jdt-language-server}";
                    };
                    buildInputs = libs;
                };
            }
        );
    };
}
