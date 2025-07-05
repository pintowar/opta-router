import { fireEvent, render, screen } from "@testing-library/vue";
import { OhVueIcon } from "oh-vue-icons";
import InputSearch from "../InputSearch.vue";

const push = vi.fn();
vi.mock("vue-router", () => ({
  useRoute: vi.fn(() => ({
    query: {},
  })),
  useRouter: vi.fn(() => ({
    push,
  })),
}));

describe("InputSearch", () => {
  it("renders the input with the query", () => {
    render(InputSearch, {
      props: {
        query: "test",
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    expect(screen.getByPlaceholderText("Search...")).toHaveValue("test");
  });

  it("navigates on enter", async () => {
    render(InputSearch, {
      props: {
        query: "",
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    const input = screen.getByPlaceholderText("Search...");
    await fireEvent.update(input, "new search");
    await fireEvent.keyPress(input, { key: "Enter", code: "Enter" });
    expect(push).toHaveBeenCalledWith({
      query: {
        q: "new search",
      },
    });
  });

  it("clears the search", async () => {
    render(InputSearch, {
      props: {
        query: "test",
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    await fireEvent.click(screen.getByTestId("clear-button"));
    expect(push).toHaveBeenCalledWith({
      query: {
        q: "",
      },
    });
  });
});
