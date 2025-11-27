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
 * Delete request.
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

/**
 * Put request.
 *
 * @param url The URL to send the put request to.
 * @param body The body of the put request.
 * @returns The response data as a JSON object.
 * @throws An error if the put request fails.
 */
export async function putRequest(url: string, body: any) {
  const res = await fetch(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error(`Failed to perform put request: ${res.status}`);
  }

  const data = await res.json();
  return data;
}

/**
 * Post request.
 *
 * @param url The URL to send the post request to.
 * @param body The body of the post request.
 * @returns The response data as a JSON object.
 * @throws An error if the post request fails.
 */
export async function postRequest(url: string, body: any) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error(`Failed to perform post request: ${res.status}`);
  }

  const data = await res.json();
  return data;
}
