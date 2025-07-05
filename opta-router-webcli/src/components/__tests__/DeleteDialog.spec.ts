import { fireEvent, render, screen } from "@testing-library/vue";
import DeleteDialog from "../DeleteDialog.vue";

import { ref } from "vue";

vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(() => ({
    delete: vi.fn(() => ({
      isFetching: ref(false),
      error: ref(null),
      execute: vi.fn(() => Promise.resolve()),
    })),
  })),
}));

describe("DeleteDialog", () => {
  it("renders the message", () => {
    render(DeleteDialog, {
      props: {
        open: true,
        message: "Are you sure?",
        url: "http://localhost/items/1",
      },
    });
    expect(screen.getByText("Are you sure?")).toBeInTheDocument();
  });

  it("emits successRemove on successful deletion", async () => {
    const { emitted } = render(DeleteDialog, {
      props: {
        open: true,
        message: "Are you sure?",
        url: "http://localhost/items/1",
      },
    });

    await fireEvent.click(screen.getByText("Delete"));
    expect(emitted().successRemove).toBeTruthy();
  });

  it.skip("emits update:open when closed", async () => {
    const { emitted } = render(DeleteDialog, {
      props: {
        open: true,
        message: "Are you sure?",
        url: "http://localhost/items/1",
      },
    });

    await fireEvent.click(screen.getByText("Close"));
    expect(emitted()["update:open"][0]).toEqual([false]);
  });
});
