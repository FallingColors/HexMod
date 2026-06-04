param(
    [string]$SshFile = "D:\project\bots\ssh\ssh.txt"
)

$ErrorActionPreference = "Stop"

$python = "C:\Users\artem\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
if (!(Test-Path $python)) {
    $python = "python"
}

$script = @'
import pathlib
import posixpath
import shlex
import hashlib
import json
import sys
import xml.etree.ElementTree as ET
import zipfile

import paramiko

ssh_file = pathlib.Path(sys.argv[1])
parts = ssh_file.read_text(encoding="utf-8").strip().split(None, 1)
if len(parts) != 2:
    raise SystemExit("SSH file must contain '<user@host> <password>'")

target, password = parts
user, host = target.split("@", 1) if "@" in target else ("root", target)

artifacts = [
    ("architectury-plugin", "architectury-plugin.gradle.plugin", "3.4.160", "https://maven.architectury.dev"),
    ("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.72", "https://repo.maven.apache.org/maven2"),
    ("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.jvm.gradle.plugin", "2.0.21", "https://plugins.gradle.org/m2"),
    ("org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.0.21", "https://repo.maven.apache.org/maven2"),
    ("org.jetbrains.kotlin", "kotlin-reflect", "1.3.72", "https://repo.maven.apache.org/maven2"),
    ("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext", "gradle-idea-ext", "0.10", "https://plugins.gradle.org/m2"),
    ("com.gradleup.shadow", "com.gradleup.shadow.gradle.plugin", "8.3.6", "https://plugins.gradle.org/m2"),
    ("com.gradleup.shadow", "shadow-gradle-plugin", "8.3.6", "https://plugins.gradle.org/m2"),
    ("dev.architectury", "architectury-transformer", "5.2.87", "https://maven.architectury.dev"),
    ("dev.architectury.loom", "dev.architectury.loom.gradle.plugin", "1.13.469", "https://maven.architectury.dev"),
    ("dev.architectury", "architectury-loom", "1.13.469", "https://maven.architectury.dev"),
    ("dev.architectury", "tiny-remapper", "1.1.0", "https://maven.architectury.dev"),
    ("dev.architectury", "mercury", "0.4.3.18", "https://maven.architectury.dev"),
    ("com.google.code.gson", "gson", "2.8.5", "https://repo.maven.apache.org/maven2"),
    ("com.google.code.gson", "gson", "2.10.1", "https://repo.maven.apache.org/maven2"),
    ("com.google.guava", "guava", "32.1.2-jre", "https://repo.maven.apache.org/maven2"),
    ("com.google.code.findbugs", "jsr305", "3.0.2", "https://repo.maven.apache.org/maven2"),
    ("org.apache.groovy", "groovy-bom", "4.0.22", "https://repo.maven.apache.org/maven2"),
    ("org.jetbrains.kotlin", "kotlin-gradle-plugins-bom", "2.0.21", "https://repo.maven.apache.org/maven2"),
    ("org.junit", "junit-bom", "5.11.0", "https://repo.maven.apache.org/maven2"),
    ("org.junit", "junit-bom", "5.10.2", "https://repo.maven.apache.org/maven2"),
    ("org.junit", "junit-bom", "5.10.1", "https://repo.maven.apache.org/maven2"),
    ("org.junit", "junit-bom", "5.7.1", "https://repo.maven.apache.org/maven2"),
    ("org.checkerframework", "checker-qual", "3.12.0", "https://repo.maven.apache.org/maven2"),
    ("org.checkerframework", "checker-qual", "3.33.0", "https://repo.maven.apache.org/maven2"),
    ("com.google.errorprone", "error_prone_annotations", "2.7.1", "https://repo.maven.apache.org/maven2"),
    ("com.google.errorprone", "error_prone_annotations", "2.18.0", "https://repo.maven.apache.org/maven2"),
    ("com.google.j2objc", "j2objc-annotations", "1.3", "https://repo.maven.apache.org/maven2"),
    ("com.google.j2objc", "j2objc-annotations", "2.8", "https://repo.maven.apache.org/maven2"),
    ("com.fasterxml.jackson", "jackson-bom", "2.17.2", "https://repo.maven.apache.org/maven2"),
    ("com.fasterxml.woodstox", "woodstox-core", "6.5.1", "https://repo.maven.apache.org/maven2"),
    ("jakarta.platform", "jakarta.jakartaee-bom", "9.1.0", "https://repo.maven.apache.org/maven2"),
    ("org.apache.maven", "maven-bom", "4.0.0-alpha-9", "https://repo.maven.apache.org/maven2"),
    ("org.apache.maven", "maven-api-meta", "4.0.0-alpha-9", "https://repo.maven.apache.org/maven2"),
    ("org.jetbrains.kotlinx", "kotlinx-coroutines-bom", "1.6.4", "https://repo.maven.apache.org/maven2"),
    ("it.unimi.dsi", "fastutil", "8.5.12", "https://repo.maven.apache.org/maven2"),
    ("blue.endless", "jankson", "1.2.2", "https://repo.maven.apache.org/maven2"),
    ("vazkii.patchouli", "Patchouli-xplat", "1.21.1-93", "https://maven.blamejared.com"),
    ("vazkii.patchouli", "Patchouli", "1.21.1-93-NEOFORGE", "https://maven.blamejared.com"),
    ("vazkii.patchouli", "Patchouli", "1.21.1-93-FABRIC", "https://maven.blamejared.com"),
    ("com.samsthenerd.inline", "inline-common", "1.21.1-1.2.2-74", "https://maven.blamejared.com"),
    ("com.samsthenerd.inline", "inline-fabric", "1.21.1-1.2.2-74", "https://maven.blamejared.com"),
    ("dev.emi", "emi-xplat-intermediary", "1.1.18+1.21.1", "https://maven.terraformersmc.com/releases", ["api"]),
    ("io.wispforest", "accessories-common", "1.1.0-beta.16+1.21.1", "https://maven.wispforest.io/releases"),
    ("io.wispforest", "accessories-fabric", "1.1.0-beta.16+1.21.1", "https://maven.wispforest.io/releases"),
    ("net.fabricmc", "fabric-language-kotlin", "1.12.3+kotlin.2.0.21", "https://maven.fabricmc.net"),
    ("org.ladysnake.cardinal-components-api", "cardinal-components-base", "6.1.3", "https://maven.ladysnake.org/releases"),
    ("org.ladysnake.cardinal-components-api", "cardinal-components-entity", "6.1.3", "https://maven.ladysnake.org/releases"),
    ("org.ladysnake.cardinal-components-api", "cardinal-components-item", "6.1.3", "https://maven.ladysnake.org/releases"),
    ("org.ladysnake.cardinal-components-api", "cardinal-components-block", "6.1.3", "https://maven.ladysnake.org/releases"),
    ("com.terraformersmc", "modmenu", "7.0.1", "https://maven.terraformersmc.com/releases"),
    ("maven.modrinth", "lithium", "E5eJVp4O", "https://api.modrinth.com/maven"),
    ("dev.emi", "emi-fabric", "1.1.18+1.21.1", "https://maven.terraformersmc.com/releases", ["api"]),
    ("me.shedaniel.cloth", "cloth-config", "15.0.130", "https://maven.shedaniel.me"),
    ("net.fabricmc.fabric-api", "fabric-api", "0.100.1+1.21", "https://maven.fabricmc.net"),
    ("net.fabricmc", "intermediary", "1.21.1", "https://maven.fabricmc.net", ["v2"]),
    ("io.wispforest", "endec", "0.1.8", "https://maven.wispforest.io/releases"),
    ("io.wispforest.endec", "gson", "0.1.5", "https://maven.wispforest.io/releases"),
    ("io.wispforest.endec", "jankson", "0.1.5", "https://maven.wispforest.io/releases"),
    ("io.wispforest.endec", "netty", "0.1.4", "https://maven.wispforest.io/releases"),
    ("io.wispforest", "owo-lib", "0.12.15+1.21", "https://maven.wispforest.io/releases"),
    ("me.shedaniel.cloth", "basic-math", "0.6.1", "https://maven.shedaniel.me"),
    ("me.shedaniel.cloth", "cloth-config-fabric", "15.0.140", "https://maven.shedaniel.me"),
    ("com.github.Virtuoel", "Pehkui", "3.8.3", "https://jitpack.io"),
    ("net.neoforged", "neoforge", "21.1.172", "https://maven.neoforged.net/releases", ["userdev"]),
    ("net.fabricmc", "fabric-loader", "0.16.14", "https://maven.fabricmc.net"),
    ("de.oceanlabs.mcp", "mcinjector", "3.8.0", "https://repo.spongepowered.org/repository/maven-public"),
    ("net.minecraftforge", "DiffPatch", "2.0.7", "https://repo.spongepowered.org/repository/maven-public"),
    ("org.apache.logging.log4j", "log4j-api", "2.24.1", "https://repo.maven.apache.org/maven2"),
    ("com.mojang", "logging", "1.2.7", "https://libraries.minecraft.net"),
    ("org.apache.logging.log4j", "log4j-api", "2.22.1", "https://repo.maven.apache.org/maven2"),
    ("org.apache.logging.log4j", "log4j-core", "2.22.1", "https://repo.maven.apache.org/maven2"),
    ("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.22.1", "https://repo.maven.apache.org/maven2"),
    ("org.jline", "jline-reader", "3.20.0", "https://repo.maven.apache.org/maven2"),
    ("org.jline", "jline-terminal", "3.20.0", "https://repo.maven.apache.org/maven2"),
    ("commons-io", "commons-io", "2.15.1", "https://repo.maven.apache.org/maven2"),
    ("org.codehaus.plexus", "plexus-utils", "3.3.0", "https://repo.maven.apache.org/maven2"),
    ("net.sf.jopt-simple", "jopt-simple", "5.0.4", "https://repo.maven.apache.org/maven2"),
    ("com.electronwill.night-config", "core", "3.8.2", "https://repo.maven.apache.org/maven2"),
    ("com.electronwill.night-config", "toml", "3.8.2", "https://repo.maven.apache.org/maven2"),
    ("com.machinezoo.noexception", "noexception", "1.7.1", "https://repo.maven.apache.org/maven2"),
    ("cpw.mods", "bootstraplauncher", "2.0.2", "https://maven.neoforged.net/releases"),
    ("cpw.mods", "modlauncher", "11.0.4", "https://maven.neoforged.net/releases"),
    ("cpw.mods", "securejarhandler", "3.0.8", "https://maven.neoforged.net/releases"),
    ("net.fabricmc", "sponge-mixin", "0.15.2+mixin.0.8.7", "https://maven.fabricmc.net"),
    ("net.jodah", "typetools", "0.6.3", "https://repo.maven.apache.org/maven2"),
    ("net.minecrell", "terminalconsoleappender", "1.3.0", "https://repo.maven.apache.org/maven2"),
    ("net.neoforged.accesstransformers", "at-modlauncher", "10.0.1", "https://maven.neoforged.net/releases"),
    ("net.neoforged.fancymodloader", "earlydisplay", "4.0.39", "https://maven.neoforged.net/releases"),
    ("net.neoforged.fancymodloader", "loader", "4.0.39", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "JarJarFileSystems", "0.4.1", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "JarJarMetadata", "0.4.1", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "JarJarSelector", "0.4.1", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "accesstransformers", "10.0.1", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "bus", "8.0.2", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "coremods", "7.0.3", "https://maven.neoforged.net/releases"),
    ("net.neoforged", "mergetool", "2.0.0", "https://maven.neoforged.net/releases", ["api"]),
    ("org.antlr", "antlr4-runtime", "4.13.1", "https://repo.maven.apache.org/maven2"),
    ("org.apache.commons", "commons-lang3", "3.14.0", "https://repo.maven.apache.org/maven2"),
    ("org.openjdk.nashorn", "nashorn-core", "15.4", "https://repo.maven.apache.org/maven2"),
    ("org.slf4j", "slf4j-api", "2.0.9", "https://repo.maven.apache.org/maven2"),
    ("commons-beanutils", "commons-beanutils", "1.9.3", "https://repo.maven.apache.org/maven2"),
    ("commons-collections", "commons-collections", "3.2.2", "https://repo.maven.apache.org/maven2"),
    ("commons-logging", "commons-logging", "1.2", "https://repo.maven.apache.org/maven2"),
    ("com.opencsv", "opencsv", "4.4", "https://repo.maven.apache.org/maven2"),
    ("de.siegmar", "fastcsv", "2.0.0", "https://repo.maven.apache.org/maven2"),
    ("net.neoforged.installertools", "cli-utils", "2.1.2", "https://maven.neoforged.net/releases"),
    ("net.neoforged.installertools", "installertools", "2.1.2", "https://maven.neoforged.net/releases"),
    ("net.neoforged.installertools", "jarsplitter", "2.1.2", "https://maven.neoforged.net/releases"),
    ("org.apache.commons", "commons-collections4", "4.2", "https://repo.maven.apache.org/maven2"),
    ("org.apache.commons", "commons-lang3", "3.8.1", "https://repo.maven.apache.org/maven2"),
    ("org.apache.commons", "commons-text", "1.3", "https://repo.maven.apache.org/maven2"),
]

extra_files = [
    ("net.neoforged", "neoform", "1.21.1-20240808.144430", "https://maven.neoforged.net/releases", "zip", None),
    ("net.neoforged", "neoforge", "21.1.172", "https://maven.neoforged.net/releases", "jar", "installer"),
    ("net.neoforged", "neoforge", "21.1.172", "https://maven.neoforged.net/releases", "jar", "universal"),
    ("net.neoforged", "mergetool", "2.0.3", "https://maven.neoforged.net/releases", "jar", "fatjar"),
    ("net.neoforged", "AutoRenamingTool", "2.0.3", "https://maven.neoforged.net/releases", "jar", "all"),
    ("net.neoforged.installertools", "installertools", "2.1.2", "https://maven.neoforged.net/releases", "jar", "fatjar"),
    ("net.neoforged.installertools", "binarypatcher", "2.1.2", "https://maven.neoforged.net/releases", "jar", "fatjar"),
]

repo_by_group = [
    ("architectury-plugin", "https://maven.architectury.dev"),
    ("dev.architectury", "https://maven.architectury.dev"),
    ("net.fabricmc", "https://maven.fabricmc.net"),
    ("net.fabricmc.fabric-api", "https://maven.fabricmc.net"),
    ("me.zeroeightsix", "https://maven.fabricmc.net"),
    ("io.wispforest", "https://maven.wispforest.io/releases"),
    ("io.wispforest.endec", "https://maven.wispforest.io/releases"),
    ("me.shedaniel", "https://maven.shedaniel.me"),
    ("com.github.Virtuoel", "https://jitpack.io"),
    ("net.neoforged", "https://maven.neoforged.net/releases"),
    ("cpw.mods", "https://maven.neoforged.net/releases"),
    ("org.ladysnake.cardinal-components-api", "https://maven.ladysnake.org/releases"),
    ("com.terraformersmc", "https://maven.terraformersmc.com/releases"),
    ("maven.modrinth", "https://api.modrinth.com/maven"),
    ("de.oceanlabs.mcp", "https://repo.spongepowered.org/repository/maven-public"),
    ("net.minecraftforge", "https://repo.spongepowered.org/repository/maven-public"),
    ("com.mojang", "https://libraries.minecraft.net"),
    ("vazkii.patchouli", "https://maven.blamejared.com"),
    ("com.samsthenerd.inline", "https://maven.blamejared.com"),
    ("dev.emi", "https://maven.terraformersmc.com/releases"),
    ("dev.architectury.loom", "https://maven.architectury.dev"),
]

local_m2 = pathlib.Path.home() / ".m2" / "repository"
gradle_file_cache = pathlib.Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
remote_root = "/tmp/hexcasting-maven-prefetch"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=15, banner_timeout=15, auth_timeout=15)
sftp = client.open_sftp()

def run(command: str) -> str:
    stdin, stdout, stderr = client.exec_command(command, timeout=120)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    if code != 0:
        raise RuntimeError(f"remote command failed ({code}): {command}\n{err}")
    return out

def repo_for(group: str, fallback: str) -> str:
    for prefix, repo in repo_by_group:
        if group == prefix or group.startswith(prefix + "."):
            return repo
    return "https://repo.maven.apache.org/maven2"

def relpath(group: str, artifact: str, version: str) -> str:
    return "/".join(group.split(".")) + f"/{artifact}/{version}"

def valid_jar(path: pathlib.Path) -> bool:
    try:
        with zipfile.ZipFile(path) as jar:
            jar.testzip()
        return True
    except zipfile.BadZipFile:
        return False

def cache_in_gradle(group: str, artifact: str, version: str, path: pathlib.Path) -> None:
    digest = hashlib.sha1(path.read_bytes()).hexdigest()
    target = gradle_file_cache / group / artifact / version / digest / path.name
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists() or target.stat().st_size != path.stat().st_size:
        target.write_bytes(path.read_bytes())
        print(f"gradle cached {target}")

def snapshot_remote_name(group: str, artifact: str, version: str, repo: str, ext: str, classifier: str | None) -> str | None:
    if not version.endswith("SNAPSHOT"):
        return None
    rel = relpath(group, artifact, version)
    metadata_name = "maven-metadata.xml"
    metadata_file = fetch_raw(repo, rel, metadata_name)
    root = ET.parse(metadata_file).getroot()
    classifier_text = classifier or ""
    for snap in root.findall(".//snapshotVersion"):
        extension = snap.findtext("extension")
        snap_classifier = snap.findtext("classifier") or ""
        value = snap.findtext("value")
        if extension == ext and snap_classifier == classifier_text and value:
            suffix = f"-{classifier}" if classifier else ""
            return f"{artifact}-{value}{suffix}.{ext}"
    timestamp = root.findtext(".//timestamp")
    build = root.findtext(".//buildNumber")
    if timestamp and build:
        base = version[:-len("-SNAPSHOT")]
        suffix = f"-{classifier}" if classifier else ""
        return f"{artifact}-{base}-{timestamp}-{build}{suffix}.{ext}"
    return None

def fetch_raw(repo: str, rel: str, filename: str) -> pathlib.Path:
    local_dir = local_m2 / pathlib.Path(rel)
    local_dir.mkdir(parents=True, exist_ok=True)
    local_file = local_dir / filename
    if local_file.exists() and local_file.stat().st_size > 0:
        print(f"local ok {rel}/{filename}")
        return local_file

    remote_dir = posixpath.join(remote_root, rel)
    run(f"mkdir -p {shlex.quote(remote_dir)}")
    url = f"{repo}/{rel}/{filename}"
    remote_file = posixpath.join(remote_dir, filename)
    curl = (
        "curl -L --fail --retry 3 --retry-all-errors "
        "--connect-timeout 15 --max-time 90 "
        f"-o {shlex.quote(remote_file)} {shlex.quote(url)}"
    )
    print(f"remote download {url}")
    run(curl)
    sftp.get(remote_file, str(local_file))
    print(f"stored {local_file} ({local_file.stat().st_size} bytes)")
    return local_file

def fetch_file(group: str, artifact: str, version: str, repo: str, ext: str, classifier: str | None = None) -> pathlib.Path | None:
    rel = relpath(group, artifact, version)
    local_dir = local_m2 / pathlib.Path(rel)
    local_dir.mkdir(parents=True, exist_ok=True)
    classifier_suffix = f"-{classifier}" if classifier else ""
    local_file = local_dir / f"{artifact}-{version}{classifier_suffix}.{ext}"

    if local_file.exists() and local_file.stat().st_size > 0:
        if ext != "jar" or valid_jar(local_file):
            print(f"local ok {rel}/{local_file.name}")
            cache_in_gradle(group, artifact, version, local_file)
            return local_file
        print(f"local corrupt {rel}/{local_file.name}; refetching")
        local_file.unlink()

    remote_dir = posixpath.join(remote_root, rel)
    run(f"mkdir -p {shlex.quote(remote_dir)}")
    remote_name = snapshot_remote_name(group, artifact, version, repo, ext, classifier) or local_file.name
    url = f"{repo}/{rel}/{remote_name}"
    remote_file = posixpath.join(remote_dir, remote_name)
    curl = (
        "curl -L --fail --retry 3 --retry-all-errors "
        "--connect-timeout 15 --max-time 90 "
        f"-o {shlex.quote(remote_file)} {shlex.quote(url)}"
    )
    print(f"remote download {url}")
    try:
        run(curl)
    except RuntimeError as exc:
        if ext == "jar":
            print(f"jar unavailable {group}:{artifact}:{version}; treating as pom-only")
            return None
        raise exc
    sftp.get(remote_file, str(local_file))
    print(f"stored {local_file} ({local_file.stat().st_size} bytes)")
    cache_in_gradle(group, artifact, version, local_file)
    return local_file

def text_of(node, name: str) -> str | None:
    child = node.find(f"m:{name}", NS)
    return child.text.strip() if child is not None and child.text else None

def resolve(value: str | None, props: dict[str, str]) -> str | None:
    if not value:
        return value
    previous = None
    while previous != value:
        previous = value
        for key, prop_value in props.items():
            value = value.replace("${" + key + "}", prop_value)
    return value

NS = {"m": "http://maven.apache.org/POM/4.0.0"}
seen = set()

def fetch_artifact(group: str, artifact: str, version: str, repo: str, want_jar: bool = True, depth: int = 0, classifiers: list[str] | None = None) -> None:
    group = resolve(group, {}) or group
    artifact = resolve(artifact, {}) or artifact
    version = resolve(version, {}) or version
    repo = repo_for(group, repo)
    key = (group, artifact, version, want_jar)
    if key in seen:
        return
    seen.add(key)

    pom_file = fetch_file(group, artifact, version, repo, "pom")
    if want_jar:
        fetch_file(group, artifact, version, repo, "jar")
        for classifier in classifiers or []:
            fetch_file(group, artifact, version, repo, "jar", classifier)

    try:
        root = ET.parse(pom_file).getroot()
    except ET.ParseError as exc:
        print(f"pom parse skipped {group}:{artifact}:{version}: {exc}")
        return

    parent = root.find("m:parent", NS)
    props = {
        "project.groupId": group,
        "pom.groupId": group,
        "project.artifactId": artifact,
        "pom.artifactId": artifact,
        "project.version": version,
        "pom.version": version,
    }

    if parent is not None:
        parent_group = text_of(parent, "groupId")
        parent_artifact = text_of(parent, "artifactId")
        parent_version = text_of(parent, "version")
        if parent_group and parent_artifact and parent_version:
            parent_repo = repo_for(parent_group, repo)
            fetch_artifact(parent_group, parent_artifact, parent_version, parent_repo, want_jar=False, depth=depth + 1)
            props["project.parent.groupId"] = parent_group
            props["pom.parent.groupId"] = parent_group
            props["project.parent.version"] = parent_version
            props["pom.parent.version"] = parent_version

    properties = root.find("m:properties", NS)
    if properties is not None:
        for child in list(properties):
            tag = child.tag.rsplit("}", 1)[-1]
            if child.text:
                props[tag] = resolve(child.text.strip(), props) or child.text.strip()

    dependency_management = root.find("m:dependencyManagement/m:dependencies", NS)
    if dependency_management is not None and depth < 6:
        for dep in dependency_management.findall("m:dependency", NS):
            scope = text_of(dep, "scope") or "compile"
            dep_type = text_of(dep, "type") or "jar"
            if scope != "import" or dep_type != "pom":
                continue
            dep_group = resolve(text_of(dep, "groupId"), props)
            dep_artifact = resolve(text_of(dep, "artifactId"), props)
            dep_version = resolve(text_of(dep, "version"), props)
            if not dep_group or not dep_artifact or not dep_version or dep_version.startswith("${"):
                print(f"import skipped {group}:{artifact}:{version} -> {dep_group}:{dep_artifact}:{dep_version}")
                continue
            fetch_artifact(dep_group, dep_artifact, dep_version, repo_for(dep_group, repo), want_jar=False, depth=depth + 1)

    dependencies = root.find("m:dependencies", NS)
    if dependencies is None or depth >= 4:
        return

    for dep in dependencies.findall("m:dependency", NS):
        scope = text_of(dep, "scope") or "compile"
        optional = text_of(dep, "optional")
        if scope in ("test", "provided", "system", "import") or optional == "true":
            continue
        dep_group = resolve(text_of(dep, "groupId"), props)
        dep_artifact = resolve(text_of(dep, "artifactId"), props)
        dep_version = resolve(text_of(dep, "version"), props)
        if not dep_group or not dep_artifact or not dep_version or dep_version.startswith("${"):
            print(f"dependency skipped {group}:{artifact}:{version} -> {dep_group}:{dep_artifact}:{dep_version}")
            continue
        if dep_group == "net.neoforged" and dep_artifact == "minecraft-dependencies":
            print(f"dependency skipped {group}:{artifact}:{version} -> {dep_group}:{dep_artifact}:{dep_version} (virtual NeoForge metadata)")
            continue
        fetch_artifact(dep_group, dep_artifact, dep_version, repo_for(dep_group, repo), want_jar=True, depth=depth + 1)

def fetch_coordinate_name(name: str) -> None:
    parts = name.split(":")
    if len(parts) < 3:
        print(f"coordinate skipped {name}")
        return
    group, artifact, version = parts[:3]
    classifier = parts[3] if len(parts) > 3 else None
    classifiers = [classifier] if classifier else None
    fetch_artifact(group, artifact, version, repo_for(group, "https://repo.maven.apache.org/maven2"), classifiers=classifiers)

def fetch_minecraft_libraries() -> None:
    minecraft_info = pathlib.Path.home() / ".gradle" / "caches" / "fabric-loom" / "1.21.1" / "mojang_minecraft_info.json"
    if not minecraft_info.exists():
        print(f"minecraft libraries skipped; missing {minecraft_info}")
        return

    data = json.loads(minecraft_info.read_text(encoding="utf-8"))
    names = set()
    for library in data.get("libraries", []):
        name = library.get("name")
        if name:
            names.add(name)
    for name in sorted(names):
        fetch_coordinate_name(name)

try:
    run(f"mkdir -p {shlex.quote(remote_root)}")
    for group, artifact, version, repo, ext, classifier in extra_files:
        fetch_file(group, artifact, version, repo_for(group, repo), ext, classifier)
    for artifact_spec in artifacts:
        group, artifact, version, repo, *rest = artifact_spec
        classifiers = rest[0] if rest else None
        fetch_artifact(group, artifact, version, repo, classifiers=classifiers)
    fetch_minecraft_libraries()
finally:
    sftp.close()
    client.close()
'@

$tmp = New-TemporaryFile
Set-Content -Path $tmp -Value $script -Encoding UTF8
try {
    & $python $tmp $SshFile
} finally {
    Remove-Item -LiteralPath $tmp -Force -ErrorAction SilentlyContinue
}
