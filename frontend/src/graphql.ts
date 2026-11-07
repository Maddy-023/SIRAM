import type { Incident, IncidentInput, IncidentType } from "./types";

interface GraphqlResponse<T> {
  data?: T;
  errors?: Array<{ message: string }>;
}

async function request<T>(query: string, variables?: Record<string, unknown>): Promise<T> {
  let response: Response;
  try {
    response = await fetch("/graphql", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ query, variables }),
    });
  } catch {
    throw new Error("No se pudo conectar con el servicio. Verifica que el entorno esté iniciado.");
  }

  const result = (await response.json()) as GraphqlResponse<T>;
  if (!response.ok) {
    throw new Error(`El servicio respondió con el estado ${response.status}.`);
  }
  if (result.errors?.length) {
    throw new Error(result.errors.map((error) => error.message).join(". "));
  }
  if (!result.data) {
    throw new Error("El servicio devolvió una respuesta vacía.");
  }
  return result.data;
}

const incidentFields = `
  id
  tipo
  ubicacion { latitud longitud }
  fechaHora
`;

export async function listIncidents(tipo?: IncidentType): Promise<Incident[]> {
  const data = await request<{ incidentes: Incident[] }>(
    `query ListarIncidentes($tipo: TipoIncidente) {
      incidentes(tipo: $tipo) { ${incidentFields} }
    }`,
    { tipo: tipo || null },
  );
  return data.incidentes;
}

export async function createIncident(input: IncidentInput): Promise<Incident> {
  const data = await request<{ reportarIncidente: Incident }>(
    `mutation ReportarIncidente($input: ReportarIncidenteInput!) {
      reportarIncidente(input: $input) { ${incidentFields} }
    }`,
    { input },
  );
  return data.reportarIncidente;
}
