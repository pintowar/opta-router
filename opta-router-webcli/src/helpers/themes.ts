const themesCategory = new Map<string, string[]>([
  [
    "light",
    [
      "light",
      "cupcake",
      "bumblebee",
      "emerald",
      "corporate",
      "retro",
      "cyberpunk",
      "valentine",
      "garden",
      "aqua",
      "lofi",
      "pastel",
      "fantasy",
      "wireframe",
      "cmyk",
      "autumn",
      "acid",
      "lemonade",
      "winter",
    ],
  ],
  ["dark", ["dark", "synthwave", "halloween", "forest", "black", "dracula", "luxury", "business", "night", "coffee"]],
]);

const categories: Record<string, "light" | "dark"> = [...themesCategory.keys()].reduce(
  (acc, cat) => ({
    ...acc,
    ...themesCategory.get(cat)?.reduce((ac, theme) => ({ ...ac, [theme]: cat }), {}),
  }),
  {}
);

const localThemes = Object.keys(categories).sort();

const modes: Record<string, string> = localThemes.reduce(
  (acc, mode) => ({
    ...acc,
    [mode]: mode,
  }),
  {}
);

export { categories, localThemes, modes };
