import type { Incident, IncidentInput, IncidentType } from "./types";

interface GraphqlResponse<T> {
  data?: T;
  errors?: Array<{ message: string }>;
}

const requestErrorMessage =
  "No pudimos comunicarnos con el sistema en este momento. Inténtalo nuevamente en unos minutos.";

async function request<T>(query: string, variables?: Record<string, unknown>): Promise<T> {
  let response: Response;
  try {
    response = await fetch("/graphql", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ query, variables }),
    });
  } catch {
    throw new Error(requestErrorMessage);
  }

  if (!response.ok) {
    throw new Error(requestErrorMessage);
  }

  let result: GraphqlResponse<T>;
  try {
    result = (await response.json()) as GraphqlResponse<T>;
  } catch {
    throw new Error(requestErrorMessage);
  }
  if (result.errors?.length || !result.data) {
    throw new Error(requestErrorMessage);
  }
  return result.data;
}

const incidentFields = `
  id
  tipo
  ubicacion { latitud longitud }
  fechaHora
`;

export async function listIncidents(tipo?: IncidentType, fecha?: string): Promise<Incident[]> {
  const data = await request<{ incidentes: Incident[] }>(
    `query ListarIncidentes($tipo: TipoIncidente, $fecha: String) {
      incidentes(tipo: $tipo, fecha: $fecha) { ${incidentFields} }
    }`,
    { tipo: tipo || null, fecha: fecha || null },
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
