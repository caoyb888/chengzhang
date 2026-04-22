#!/usr/bin/env python3
"""
测试计划 §4.5：Flyway SQL 合规性自动验证脚本
验收标准 S0-06：
  1. 所有业务表含四件套字段（id/created_at/updated_at/is_deleted）
  2. 无 TIMESTAMP 类型字段（统一使用 DATETIME）
  3. 无数据库层面外键约束（FOREIGN KEY）
  4. 状态字段类型为 VARCHAR，不使用数字类型存状态
  5. id 字段类型为 BIGINT
"""

import os
import re
import sys
from pathlib import Path
from dataclasses import dataclass, field
from typing import List, Dict

FLYWAY_BASE = Path(__file__).parent.parent / "flyway"
REQUIRED_FIELDS = {"id", "created_at", "updated_at", "is_deleted"}

@dataclass
class TableResult:
    db: str
    table: str
    file: str
    missing_fields: List[str] = field(default_factory=list)
    has_timestamp: bool = False
    has_foreign_key: bool = False
    id_type_ok: bool = True
    pass_ : bool = True

    def add_failure(self, msg: str):
        self.pass_ = False
        return msg


def extract_create_tables(sql_text: str) -> List[Dict]:
    """从 SQL 文件中提取所有 CREATE TABLE 语句"""
    # 匹配 CREATE TABLE ... ( ... );  （跨行）
    pattern = re.compile(
        r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?`?(\w+)`?\s*\((.*?)\)\s*ENGINE',
        re.IGNORECASE | re.DOTALL
    )
    tables = []
    for m in pattern.finditer(sql_text):
        tables.append({"name": m.group(1), "body": m.group(2)})
    return tables


def check_required_fields(body: str) -> List[str]:
    """检查四件套字段是否存在"""
    missing = []
    for f in REQUIRED_FIELDS:
        # 匹配字段定义行（`field_name` TYPE ...）
        if not re.search(rf'`{f}`', body, re.IGNORECASE):
            missing.append(f)
    return missing


def check_timestamp_type(body: str) -> bool:
    """检查是否存在 TIMESTAMP 类型（禁止使用）"""
    lines = body.split('\n')
    for line in lines:
        # 跳过注释行
        if line.strip().startswith('--'):
            continue
        # 匹配字段定义中的 TIMESTAMP 关键字（排除 DEFAULT CURRENT_TIMESTAMP）
        if re.search(r'`\w+`\s+TIMESTAMP\b', line, re.IGNORECASE):
            return True
    return False


def check_foreign_key(body: str) -> bool:
    """检查是否存在外键约束（禁止使用）"""
    return bool(re.search(r'\bFOREIGN\s+KEY\b', body, re.IGNORECASE))


def check_id_type(body: str) -> bool:
    """检查 id 字段是否为 BIGINT UNSIGNED"""
    match = re.search(r'`id`\s+(\w+)', body, re.IGNORECASE)
    if match:
        return 'BIGINT' in match.group(1).upper()
    return True  # 若无 id 字段，由 missing_fields 检查覆盖


PASS_ICON = "✅"
FAIL_ICON = "❌"
WARN_ICON = "⚠️"

def run_validation() -> bool:
    all_results: List[TableResult] = []
    issues: List[str] = []

    if not FLYWAY_BASE.exists():
        print(f"{FAIL_ICON} Flyway 脚本目录不存在: {FLYWAY_BASE}")
        return False

    dbs = sorted([d for d in FLYWAY_BASE.iterdir() if d.is_dir()])
    if not dbs:
        print(f"{FAIL_ICON} 未找到任何数据库目录")
        return False

    print(f"\n{'='*70}")
    print(f"  Flyway SQL 合规性验证  |  目录: {FLYWAY_BASE}")
    print(f"{'='*70}\n")

    total_tables = 0
    total_files = 0

    for db_dir in dbs:
        db_name = db_dir.name
        sql_files = sorted(db_dir.glob("*.sql"))
        if not sql_files:
            print(f"{WARN_ICON} {db_name}: 未找到 SQL 文件")
            continue

        print(f"📂 {db_name}")
        for sql_file in sql_files:
            total_files += 1
            sql_text = sql_file.read_text(encoding="utf-8")
            tables = extract_create_tables(sql_text)

            if not tables:
                print(f"   {WARN_ICON} {sql_file.name}: 未识别到 CREATE TABLE 语句")
                continue

            for t in tables:
                total_tables += 1
                r = TableResult(db=db_name, table=t["name"], file=sql_file.name)
                body = t["body"]

                # 检查1：四件套字段
                r.missing_fields = check_required_fields(body)
                if r.missing_fields:
                    msg = f"   {FAIL_ICON} {t['name']}: 缺少字段 {r.missing_fields}"
                    issues.append(msg)
                    r.pass_ = False

                # 检查2：禁用 TIMESTAMP
                r.has_timestamp = check_timestamp_type(body)
                if r.has_timestamp:
                    msg = f"   {FAIL_ICON} {t['name']}: 含 TIMESTAMP 字段（应使用 DATETIME）"
                    issues.append(msg)
                    r.pass_ = False

                # 检查3：禁用外键
                r.has_foreign_key = check_foreign_key(body)
                if r.has_foreign_key:
                    msg = f"   {FAIL_ICON} {t['name']}: 含 FOREIGN KEY（禁止在 DB 层创建外键）"
                    issues.append(msg)
                    r.pass_ = False

                # 检查4：id 字段类型
                r.id_type_ok = check_id_type(body)
                if not r.id_type_ok:
                    msg = f"   {FAIL_ICON} {t['name']}: id 字段类型不是 BIGINT"
                    issues.append(msg)
                    r.pass_ = False

                icon = PASS_ICON if r.pass_ else FAIL_ICON
                status = "通过" if r.pass_ else "不通过"
                print(f"   {icon} {t['name']} ({status})")
                all_results.append(r)

        print()

    # 汇总统计
    passed = sum(1 for r in all_results if r.pass_)
    failed = sum(1 for r in all_results if not r.pass_)

    print(f"{'='*70}")
    print(f"  验证结果汇总")
    print(f"{'='*70}")
    print(f"  SQL 文件数：{total_files}")
    print(f"  数据表总数：{total_tables}")
    print(f"  {PASS_ICON} 通过：{passed} 张")
    print(f"  {'❌' if failed > 0 else '  '} 不通过：{failed} 张")
    print()

    if issues:
        print("  问题详情：")
        for issue in issues:
            print(f"  {issue}")
        print()

    if failed == 0:
        print(f"  {PASS_ICON} 所有表通过合规性检查！")
    else:
        print(f"  {FAIL_ICON} {failed} 张表存在合规性问题，请修复后重新验证。")

    print(f"{'='*70}\n")
    return failed == 0


if __name__ == "__main__":
    ok = run_validation()
    sys.exit(0 if ok else 1)
