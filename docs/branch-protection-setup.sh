#!/usr/bin/env bash
# ============================================================
# 宸章论文系统 — GitHub 分支保护规则配置脚本
# 对应 CLAUDE.md §七 分支策略
#
# 使用方式：
#   chmod +x branch-protection-setup.sh
#   GITHUB_TOKEN=ghp_xxx GITHUB_OWNER=caoyb888 GITHUB_REPO=chengzhang ./branch-protection-setup.sh
# ============================================================

set -e

: "${GITHUB_TOKEN:?请设置 GITHUB_TOKEN 环境变量}"
: "${GITHUB_OWNER:?请设置 GITHUB_OWNER 环境变量}"
: "${GITHUB_REPO:?请设置 GITHUB_REPO 环境变量}"

API="https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}"
AUTH="Authorization: Bearer ${GITHUB_TOKEN}"
CT="Content-Type: application/json"

echo ">>> 配置 main 分支保护规则..."
curl -s -X PUT "${API}/branches/main/protection" \
  -H "${AUTH}" -H "${CT}" \
  -d '{
    "required_status_checks": {
      "strict": true,
      "contexts": ["ci/build", "ci/test"]
    },
    "enforce_admins": true,
    "required_pull_request_reviews": {
      "dismiss_stale_reviews": true,
      "require_code_owner_reviews": false,
      "required_approving_review_count": 1
    },
    "restrictions": null,
    "allow_force_pushes": false,
    "allow_deletions": false,
    "required_linear_history": false,
    "block_creations": false
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('OK' if 'url' in d else d.get('message','error'))"

echo ">>> 配置 develop 分支保护规则..."
curl -s -X PUT "${API}/branches/develop/protection" \
  -H "${AUTH}" -H "${CT}" \
  -d '{
    "required_status_checks": {
      "strict": true,
      "contexts": ["ci/build", "ci/test"]
    },
    "enforce_admins": false,
    "required_pull_request_reviews": {
      "dismiss_stale_reviews": true,
      "require_code_owner_reviews": false,
      "required_approving_review_count": 1
    },
    "restrictions": null,
    "allow_force_pushes": false,
    "allow_deletions": false,
    "required_linear_history": false,
    "block_creations": false
  }' | python3 -c "import sys,json; d=json.load(sys.stdin); print('OK' if 'url' in d else d.get('message','error'))"

echo ""
echo "✓ 分支保护规则配置完成"
echo ""
echo "规则摘要："
echo "  main    : PR 合并必须通过 CI + 至少 1 人 Review，禁止 force push，禁止直接删除"
echo "  develop : PR 合并必须通过 CI + 至少 1 人 Review，禁止 force push"
echo ""
echo "分支流转规则（来自 CLAUDE.md §七）："
echo "  feature/* → develop  : 功能开发完成后发 PR"
echo "  release/* → main     : 发版前从 develop 拉出 release，合并 main 后打 Tag"
echo "  hotfix/*  → main + develop : 生产紧急修复，双向合并"
