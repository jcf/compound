/** @type {import('tailwindcss').Config} */
module.exports = {
  content: {
    files: ["./src/**/*.clj"],
  },
  theme: {
    extend: {},
  },
  plugins: [require("@tailwindcss/forms")],
};
