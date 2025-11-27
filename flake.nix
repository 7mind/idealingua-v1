{
  description = "idealingua-v1 build environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/25.11";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  inputs.squish-find-the-brains.url = "github:7mind/squish-find-the-brains";
  inputs.squish-find-the-brains.inputs.nixpkgs.follows = "nixpkgs";
  inputs.squish-find-the-brains.inputs.flake-utils.follows = "flake-utils";

  inputs.mudyla.url = "github:7mind/mudyla";
  inputs.mudyla.inputs.nixpkgs.follows = "nixpkgs";

  outputs =
    { self
    , nixpkgs
    , flake-utils
    , squish-find-the-brains
    , mudyla
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
        };

        versionSbt = builtins.readFile ./version.sbt;
        versionMatch = builtins.match ''.*"([0-9]+\.[0-9]+\.[0-9]+)(-SNAPSHOT)?".*'' versionSbt;
        version = builtins.elemAt versionMatch 0;

        jdk = pkgs.graalvmPackages.graalvm-ce;

        coursierCache = squish-find-the-brains.lib.mkCoursierCache {
          inherit pkgs;
          lockfilePath = ./deps.lock.json;
        };

        sbtSetup = squish-find-the-brains.lib.mkSbtSetup {
          inherit pkgs coursierCache jdk;
        };
      in
      {
        packages = rec {
          idealingua-v1 = pkgs.stdenv.mkDerivation {
            inherit version;
            pname = "idealingua-v1";
            src = ./.;
            nativeBuildInputs = sbtSetup.nativeBuildInputs ++ [ pkgs.libarchive pkgs.ammonite_2_13 ];
            inherit (sbtSetup) JAVA_HOME;

            buildPhase = ''
              ${sbtSetup.setupScript}
              amm --home $TMPDIR --tmp-output-directory --no-home-predef ./sbtgen.sc
              ${pkgs.lib.optionalString pkgs.stdenv.isDarwin ''
                HOME="$TMPDIR" \
                SBT_OPTS="-Duser.home=$TMPDIR -Dsbt.global.base=$TMPDIR/.sbt -Dsbt.ivy.home=$TMPDIR/.ivy2 -Divy.home=$TMPDIR/.ivy2 -Dsbt.boot.directory=$TMPDIR/.sbt/boot" \
                sbt "++2.13 clean" "++2.13 Universal/packageBin"
              ''}
              ${pkgs.lib.optionalString (!pkgs.stdenv.isDarwin) ''
                sbt "++2.13 clean" "++2.13 Universal/packageBin"
              ''}
            '';

            installPhase = ''
              mkdir -p $out
              bsdtar -xf ./idealingua-v1/idealingua-v1-compiler/target/universal/idealingua-v1-compiler-*.zip --strip-components 1 -C $out/
            '';
          };
          default = idealingua-v1;
        };

        devShells.default = pkgs.mkShell {
          JAVA_HOME = jdk;
          nativeBuildInputs = with pkgs.buildPackages; [
            ncurses

            jdk
            coursier
            ammonite_2_13
            pkgs.buildPackages.sbt

            dotnet-sdk_9
            mono
            msbuild
            dotnetPackages.NUnitConsole
            dotnetPackages.Nuget

            protobuf

            nodejs_24
            typescript
            yarn

            coreutils
            shellspec
            jq
            nix
            gitMinimal

            squish-find-the-brains.packages.${system}.generate-lockfile
            mudyla.packages.${system}.default
          ];
        };
      }
    );
}
