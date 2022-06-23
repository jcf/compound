/** @type {import('tailwindcss').Config} */
module.exports = {
  content: {
    files: ["./src/**/*.clj"],
    // extract: {
    //   clj: (content) => {
    //     return content.match(/[^<>"'.`\s]*[^<>"'.`\s:]/g);
    //   },
    // },
  },
  theme: {
    extend: {},
  },
  plugins: [require("@tailwindcss/forms")],
};
