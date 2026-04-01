# technical-docs

本目录存放基于当前仓库源码生成的技术文档交付物，统一放在 `docs/technical-docs/` 下。

## 目录结构

- `source/frontend.md`：前端源码级技术文档
- `source/backend.md`：后端源码级技术文档
- `assets/*.mmd`：架构图与数据流图的 Mermaid 源文件
- `tools/generate_pdfs.py`：PDF 生成脚本
- `output/frontend.pdf`：前端 PDF
- `output/backend.pdf`：后端 PDF

## 生成命令

在仓库根目录执行：

```bash
python "docs/technical-docs/tools/generate_pdfs.py"
```

## 当前环境探测结果

### 已检测到

- Python 3.11.9
- ReportLab（可用于 PDF 生成）
- Windows 字体：
  - 中文：`C:\Windows\Fonts\msyh.ttc`（Microsoft YaHei）
  - 备用中文：`C:\Windows\Fonts\simhei.ttf`
  - 英文等宽：`C:\Windows\Fonts\consola.ttf`

### 未检测到

- `pandoc`
- `typst`
- `wkhtmltopdf`
- `markdownlint`
- `textlint`

因此本次 PDF 采用 **Python + ReportLab** 生成，而不是 Pandoc/Typst 流程。

## 字体说明

用户目标要求：

- 字体不小于 10pt
- 行间距 1.15 倍
- 中文优先使用思源黑体
- 英文优先使用 Consolas

当前运行环境未检测到思源黑体，因此 PDF 实际使用：

- 中文：Microsoft YaHei（`msyh.ttc`）
- 英文等宽代码：Consolas（`consola.ttf`）

这属于“在当前环境下尽量满足”的实现。如需严格满足思源黑体要求，请先安装 Source Han Sans / 思源黑体后重新生成。

## 校验命令

### 1. 重新生成 PDF

```bash
python "docs/technical-docs/tools/generate_pdfs.py"
```

### 2. 计算 SHA256

```bash
python - <<'PY'
from pathlib import Path
import hashlib
for path in [
    Path('docs/technical-docs/source/frontend.md'),
    Path('docs/technical-docs/source/backend.md'),
    Path('docs/technical-docs/output/frontend.pdf'),
    Path('docs/technical-docs/output/backend.pdf'),
]:
    data = path.read_bytes()
    print(path.as_posix(), hashlib.sha256(data).hexdigest())
PY
```

### 3. 当前 SHA256

- `source/frontend.md`: `fa8e4d99cad8b66d14f8b58e4236738834b0e0b6ce3fc083f4cde77b27fd9a34`
- `source/backend.md`: `f80cad26162793adacb91ca31e6d2394f9496ef2d29c5eb6edfff522c93009ea`
- `output/frontend.pdf`: `322fdb28f603b7c3814278ee475fcef74248d70cd24819f48d01fbf43b23feaf`
- `output/backend.pdf`: `9c075a4badc71d4cbb2d66972ad985cccd1369f180219548616c9915a9befad5`

### 4. markdownlint / textlint

当前仓库未内置这两个工具的配置，也未检测到可执行命令，因此本次交付只能如实说明：**现状无法在本仓库内直接完成“无警告”校验**。

如果后续需要补齐，可在仓库中新增：

- `.markdownlint.json`
- `.textlintrc`
- 对应 npm 依赖与执行脚本

## 关于验收项与仓库现状

以下要求在当前仓库中未发现现成实现，因此文档中明确标注为“未发现 / 未配置”，而非虚构：

- 前后端自动化测试体系
- CI/CD 流水线
- Docker / docker-compose / 多阶段构建
- 配置中心（Nacos / Consul / ConfigMap）
- Prometheus + Grafana / Sentry / ELK
- markdownlint / textlint 配置
- Adobe Acrobat 无障碍检查满分保证

## 已知限制

1. 当前 PDF 生成链为最小可控实现，目录可点击、代码块可读、中文可正常显示，但并非完整 Pandoc 样式系统。
2. Mermaid 文件当前以源码形式保留在 `assets/`，便于后续转换为 SVG/PNG；本次 PDF 未直接渲染 Mermaid 图形。
3. 若要进一步提升 PDF 视觉质量，建议后续补装 Pandoc 或 Typst，并引入正式模板。
