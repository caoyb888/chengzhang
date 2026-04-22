import pluginVue from 'eslint-plugin-vue'
import tsEslint from '@typescript-eslint/eslint-plugin'
import tsParser from '@typescript-eslint/parser'
import vueParser from 'vue-eslint-parser'

export default [
  // 全局忽略
  {
    ignores: ['dist/**', 'node_modules/**', 'src/types/auto-imports.d.ts', 'src/types/components.d.ts'],
  },

  // Vue 文件：使用 eslint-plugin-vue flat/recommended 规则集
  // pluginVue.configs['flat/recommended'] 是配置对象数组，展开后覆盖
  ...pluginVue.configs['flat/recommended'].map((cfg: object) => ({
    ...cfg,
    files: ['**/*.vue'],
  })),

  // Vue 文件追加自定义规则
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tsParser,
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/require-default-prop': 'off',
      'vue/no-v-html': 'warn',
      'vue/component-name-in-template-casing': ['error', 'PascalCase'],
    },
  },

  // TypeScript 文件
  {
    files: ['**/*.ts', '**/*.tsx'],
    plugins: {
      '@typescript-eslint': tsEslint,
    },
    languageOptions: {
      parser: tsParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      ...tsEslint.configs.recommended.rules,
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/no-non-null-assertion': 'warn',
    },
  },

  // 全局规则（JS/TS/Vue 通用）
  {
    files: ['**/*.{js,ts,vue}'],
    rules: {
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'no-debugger': 'error',
      'prefer-const': 'error',
      'no-var': 'error',
      eqeqeq: ['error', 'always'],
    },
  },
]
