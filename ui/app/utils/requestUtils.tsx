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
 * Delete request
 *
 * @param url The URL to send the delete request to.
 * @throws An error if the deletion fails.
 */
export async function deleteRequest(url: string) {
  const res = await fetch(url, {
    method: "DELETE",
  });

  if (!res.ok) {
    throw new Error(`Failed to perform delete request: ${res.status}`);
  }
}
