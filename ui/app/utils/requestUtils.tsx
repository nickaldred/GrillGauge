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

export async function deleteHub(hubId: number) {
  const res = await fetch(`http://localhost:8080/api/v1/hub/${hubId}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    throw new Error(`Failed to delete hub: ${res.status}`);
  }
}
