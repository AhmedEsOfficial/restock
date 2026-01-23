#!/bin/bash
# Restock Android - Git Hooks Setup Script
# Run this script to install the pre-commit hooks.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GIT_HOOKS_DIR="$(git rev-parse --git-dir)/hooks"

echo "🔧 Setting up Git hooks for Restock..."

# Configure Git to use our custom hooks directory
git config core.hooksPath .githooks

# Make hooks executable
chmod +x "$SCRIPT_DIR/pre-commit"

echo ""
echo "✅ Git hooks installed successfully!"
echo ""
echo "The following hooks are now active:"
echo "  • pre-commit: Runs Ktlint and Detekt before each commit"
echo ""
echo "To skip hooks temporarily (not recommended), use:"
echo "  git commit --no-verify"
echo ""
echo "To uninstall hooks, run:"
echo "  git config --unset core.hooksPath"
