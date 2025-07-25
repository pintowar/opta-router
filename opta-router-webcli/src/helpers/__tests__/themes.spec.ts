import { describe, expect, it } from "vitest";
import { categories, localThemes, modes } from "../themes";

describe("themes", () => {
  it("should correctly categorize themes", () => {
    expect(categories.light).toBe("light");
    expect(categories.dark).toBe("dark");
    expect(categories.cupcake).toBe("light");
    expect(categories.synthwave).toBe("dark");
  });

  it("should list all themes alphabetically", () => {
    const expectedThemes = [
      "acid",
      "aqua",
      "autumn",
      "black",
      "bumblebee",
      "business",
      "cmyk",
      "coffee",
      "corporate",
      "cupcake",
      "cyberpunk",
      "dark",
      "dracula",
      "emerald",
      "fantasy",
      "forest",
      "garden",
      "halloween",
      "lemonade",
      "light",
      "lofi",
      "luxury",
      "night",
      "pastel",
      "retro",
      "synthwave",
      "valentine",
      "wireframe",
      "winter",
    ];
    const criteria = (a: string, b: string) => a.localeCompare(b);
    expect(localThemes.toSorted(criteria)).toEqual(expectedThemes.toSorted(criteria));
  });

  it("should create a modes object where each theme maps to itself", () => {
    expect(modes.light).toBe("light");
    expect(modes.dark).toBe("dark");
    expect(modes.cupcake).toBe("cupcake");
    expect(modes.synthwave).toBe("synthwave");
    expect(Object.keys(modes).length).toBe(localThemes.length);
  });
});
