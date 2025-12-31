// This file contains utility functions for making HTTP requests.
// It provides functions for GET, POST, PUT, and DELETE requests,
// handling JSON bodies and responses.

// ** Types **
type JsonBody = Record<string, unknown> | Array<unknown> | object;

/**
 * Fetches data from the specified URL.
 *
 * @param url The URL to fetch data from.
 * @returns The fetched data as a JSON object.
 * @throws An error if the fetch fails.
 */
export async function getData<TResponse = unknown>(
  url: string,
  token?: string
): Promise<TResponse> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: "GET",
    headers,
  });

  if (!response.ok) {
    throw new Error("Failed to fetch data");
  }

  return (await response.json()) as TResponse;
}

/**
 * Delete request.
 *
 * @param url The URL to send the delete request to.
 * @throws An error if the deletion fails.
 */
export async function deleteRequest(url: string, token?: string) {
  const headers: Record<string, string> = {};
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method: "DELETE",
    headers,
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
export async function putRequest<TResponse = unknown>(
  url: string,
  body: JsonBody,
  token?: string
): Promise<TResponse | string | null> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method: "PUT",
    headers,
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error(`Failed to perform put request: ${res.status}`);
  }

  const text = await res.text();
  if (!text) return null;

  try {
    return JSON.parse(text) as TResponse;
  } catch {
    return text;
  }
}

/**
 * Post request.
 *
 * @param url The URL to send the post request to.
 * @param body The body of the post request.
 * @returns The response data as a JSON object.
 * @throws An error if the post request fails.
 */
export async function postRequest<TResponse = unknown>(
  url: string,
  body: JsonBody,
  token?: string
): Promise<TResponse | string | null> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method: "POST",
    headers,
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error(`Failed to perform post request: ${res.status}`);
  }

  const text = await res.text();
  if (!text) return null;

  try {
    return JSON.parse(text) as TResponse;
  } catch {
    return text;
  }
}
