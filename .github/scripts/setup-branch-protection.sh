#!/bin/bash
# One-time branch protection setup via GitHub CLI.
# Prerequisites: gh auth login
# Usage: bash .github/scripts/setup-branch-protection.sh

set -euo pipefail

REPO="souravdas090300/aurum-core-banking"

echo "Configuring branch protection for: ${REPO}"
echo ""

# ── main branch ─────────────────────────────────────────────────────────────
echo "Setting up protection for 'main'..."
gh api "repos/${REPO}/branches/main/protection" \
  --method PUT \
  --header "Accept: application/vnd.github+json" \
  --field 'required_status_checks={"strict":true,"contexts":["unit-tests","integration-tests","security-scan","code-quality"]}' \
  --field 'enforce_admins=true' \
  --field 'required_pull_request_reviews={"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":false}' \
  --field 'restrictions=null' \
  --field 'allow_force_pushes=false' \
  --field 'allow_deletions=false'

echo "  ✅ main: CI gates + 1 reviewer required"

# ── develop branch ───────────────────────────────────────────────────────────
echo "Setting up protection for 'develop'..."
gh api "repos/${REPO}/branches/develop/protection" \
  --method PUT \
  --header "Accept: application/vnd.github+json" \
  --field 'required_status_checks={"strict":true,"contexts":["unit-tests"]}' \
  --field 'enforce_admins=false' \
  --field 'required_pull_request_reviews={"required_approving_review_count":1,"dismiss_stale_reviews":true}' \
  --field 'restrictions=null' \
  --field 'allow_force_pushes=false' \
  --field 'allow_deletions=false'

echo "  ✅ develop: unit-tests gate + 1 reviewer required"
echo ""
echo "Branch protection configured successfully for ${REPO}"
