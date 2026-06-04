param(
    [int]$ConnectTimeoutSeconds = 10,
    [int]$MaxTimeSeconds = 20
)

$ErrorActionPreference = "Continue"

$checks = @(
    @{
        Name = "Architectury plugin"
        Url = "https://maven.architectury.dev/architectury-plugin/architectury-plugin.gradle.plugin/3.4.160/architectury-plugin.gradle.plugin-3.4.160.jar"
    },
    @{
        Name = "Architectury Loom"
        Url = "https://maven.architectury.dev/dev/architectury/architectury-loom/1.13.469/architectury-loom-1.13.469.jar"
    },
    @{
        Name = "Tiny Remapper"
        Url = "https://maven.architectury.dev/dev/architectury/tiny-remapper/1.1.0/tiny-remapper-1.1.0.jar"
    },
    @{
        Name = "Mercury"
        Url = "https://maven.architectury.dev/dev/architectury/mercury/0.4.3.18/mercury-0.4.3.18.jar"
    },
    @{
        Name = "MCP Injector"
        Url = "https://repo.spongepowered.org/repository/maven-public/de/oceanlabs/mcp/mcinjector/3.8.0/mcinjector-3.8.0.jar"
    },
    @{
        Name = "DiffPatch"
        Url = "https://repo.spongepowered.org/repository/maven-public/net/minecraftforge/DiffPatch/2.0.7/DiffPatch-2.0.7.jar"
    },
    @{
        Name = "FastUtil"
        Url = "https://repo.maven.apache.org/maven2/it/unimi/dsi/fastutil/8.5.12/fastutil-8.5.12.jar"
    },
    @{
        Name = "Guava"
        Url = "https://repo.maven.apache.org/maven2/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar"
    },
    @{
        Name = "Gson"
        Url = "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    },
    @{
        Name = "Jankson"
        Url = "https://repo.maven.apache.org/maven2/blue/endless/jankson/1.2.2/jankson-1.2.2.jar"
    },
    @{
        Name = "Fabric API"
        Url = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.100.1+1.21/fabric-api-0.100.1+1.21.jar"
    }
)

foreach ($check in $checks) {
    Write-Host "== $($check.Name) =="
    Write-Host $check.Url

    $head = & curl.exe -I -L --connect-timeout $ConnectTimeoutSeconds --max-time $MaxTimeSeconds --silent --show-error $check.Url 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "HEAD failed: $head" -ForegroundColor Red
        continue
    }

    $status = ($head | Select-String -Pattern "^HTTP/" | Select-Object -Last 1).Line
    $length = ($head | Select-String -Pattern "^Content-Length:" | Select-Object -Last 1).Line
    Write-Host $status
    if ($length) { Write-Host $length }

    $probe = & curl.exe -L --range 0-65535 --connect-timeout $ConnectTimeoutSeconds --max-time $MaxTimeSeconds --output NUL --silent --show-error --write-out "probe=%{http_code} speed=%{speed_download}Bps time=%{time_total}s" $check.Url 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Probe failed: $probe" -ForegroundColor Red
    } else {
        Write-Host $probe
    }
}
