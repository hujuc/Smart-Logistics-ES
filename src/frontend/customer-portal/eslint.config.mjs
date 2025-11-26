import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const eslintConfig = [
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  {
    ignores: [
      "node_modules/**",
      ".next/**",
      "out/**",
      "build/**",
      "next-env.d.ts",
    ],
  },
  {
    rules: {
      // Relaxed rules for development
      "prefer-const": "warn", // Warning instead of error
      "@typescript-eslint/no-explicit-any": "warn", // Allow 'any' with warning
      "@typescript-eslint/no-unused-vars": ["warn", { 
        "argsIgnorePattern": "^_",
        "varsIgnorePattern": "^_" 
      }], // Allow unused vars starting with _
      "@next/next/no-html-link-for-pages": "warn", // Allow <a> tags with warning
      "react/no-unescaped-entities": "warn", // Allow apostrophes with warning
      "react-hooks/exhaustive-deps": "warn", // Dependencies warning only
    },
  },
];

export default eslintConfig;
