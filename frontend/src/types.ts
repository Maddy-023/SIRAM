export const incidentTypes = [
  "HURTO",
  "ACCIDENTE_TRANSITO",
  "INCENDIO",
  "SISMO",
  "INUNDACION",
  "OTRO",
] as const;

export type IncidentType = (typeof incidentTypes)[number];

export interface Coordinates {
  latitud: number;
  longitud: number;
}

export interface Incident {
  id: string;
  tipo: IncidentType;
  ubicacion: Coordinates;
  fechaHora: string;
}

export interface IncidentInput {
  tipo: IncidentType;
  ubicacion: Coordinates;
}
