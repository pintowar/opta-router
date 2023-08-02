const themesCategory = {
  "dark": [
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
  "light": [
      "dark",
      "synthwave",
      "halloween",
      "forest",
      "black",
      "dracula",
      "luxury",
      "business",
      "night",
      "coffee",
  ]
};

const categories = Object.keys(themesCategory).reduce((acc, cat) => ({
    ...acc,
    ...(themesCategory[cat].reduce((ac, theme) => ({...ac, [theme]: cat}), {}))
}), {});

const localThemes = Object.keys(categories).sort()

export { categories, localThemes };