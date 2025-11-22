// ui/app/utils/requestUtils.tsx
// Utility functions for making API requests.

/**
 * Fetches data from the specified URL.
 *
 * @param url The URL to fetch data from.
 * @returns The fetched data as a JSON object.
 * @throws An error if the fetch fails.
 */
export async function getData(url: string) {
  const response = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error("Failed to fetch data");
  }

  const data = await response.json();
  return data;
}

/**
 * Deletes a hub by its ID.
 *
 * @param hubId The ID of the hub to delete.
 * @throws An error if the deletion fails.
 */
export async function deleteHub(hubId: number) {
  const res = await fetch(`http://localhost:8080/api/v1/hub/${hubId}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    throw new Error(`Failed to delete hub: ${res.status}`);
  }
}
