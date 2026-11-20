import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import { createIncident, listIncidents } from "./graphql";
import { incidentTypes, type Incident, type IncidentType } from "./types";

const typeLabels: Record<IncidentType, string> = {
  HURTO: "Hurto",
  ACCIDENTE_TRANSITO: "Accidente de tránsito",
  INCENDIO: "Incendio",
  SISMO: "Sismo",
  INUNDACION: "Inundación",
  OTRO: "Otro",
};

function formatDate(value: string) {
  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function App() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [filterType, setFilterType] = useState<IncidentType | "">("");
  const [filterDate, setFilterDate] = useState("");
  const [formType, setFormType] = useState<IncidentType | "">("");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [locationAccuracy, setLocationAccuracy] = useState<number | null>(null);
  const [locating, setLocating] = useState(false);
  const [locationPromptOpen, setLocationPromptOpen] = useState(false);
  const [reportConfirmationOpen, setReportConfirmationOpen] = useState(false);
  const [loadingList, setLoadingList] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [listError, setListError] = useState("");
  const [formError, setFormError] = useState("");
  const [locationError, setLocationError] = useState("");
  const [success, setSuccess] = useState("");
  const locationDialogRef = useRef<HTMLDialogElement>(null);
  const reportDialogRef = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = locationDialogRef.current;
    if (!dialog) return;

    if (locationPromptOpen && !dialog.open) {
      dialog.showModal();
    } else if (!locationPromptOpen && dialog.open) {
      dialog.close();
    }
  }, [locationPromptOpen]);

  useEffect(() => {
    const dialog = reportDialogRef.current;
    if (!dialog) return;

    if (reportConfirmationOpen && !dialog.open) {
      dialog.showModal();
    } else if (!reportConfirmationOpen && dialog.open) {
      dialog.close();
    }
  }, [reportConfirmationOpen]);

  const refreshIncidents = useCallback(async (type: IncidentType | "", date: string) => {
    setLoadingList(true);
    setListError("");
    try {
      setIncidents(await listIncidents(type || undefined, date || undefined));
    } catch (error) {
      setListError(error instanceof Error ? error.message : "No se pudieron cargar los reportes.");
    } finally {
      setLoadingList(false);
    }
  }, []);

  useEffect(() => {
    void refreshIncidents(filterType, filterDate);
  }, [filterType, filterDate, refreshIncidents]);

  const sortedIncidents = useMemo(
    () => [...incidents].sort((a, b) => Date.parse(b.fechaHora) - Date.parse(a.fechaHora)),
    [incidents],
  );

  function handleLocate() {
    setLocationError("");
    setFormError("");
    setSuccess("");
    setLocationPromptOpen(true);
  }

  function requestLocation() {
    if (!navigator.geolocation) {
      setLatitude("");
      setLongitude("");
      setLocationAccuracy(null);
      setLocationError("Este dispositivo no puede compartir su ubicación. Sin ella no es posible enviar el reporte.");
      return;
    }

    setLatitude("");
    setLongitude("");
    setLocationAccuracy(null);
    setLocating(true);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLatitude(String(position.coords.latitude));
        setLongitude(String(position.coords.longitude));
        setLocationAccuracy(position.coords.accuracy);
        setLocating(false);
      },
      (error) => {
        setLocating(false);
        if (error.code === error.PERMISSION_DENIED) {
          setLocationError("Sin permiso para acceder a tu ubicación no es posible enviar el reporte. Activa el permiso en tu navegador y vuelve a intentarlo.");
        } else if (error.code === error.POSITION_UNAVAILABLE) {
          setLocationError("No pudimos encontrar tu ubicación. Revisa que esté activada en el dispositivo e inténtalo de nuevo.");
        } else {
          setLocationError("No pudimos obtener tu ubicación a tiempo. Acércate al lugar del incidente e inténtalo de nuevo.");
        }
      },
      { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 },
    );
  }

  function confirmLocationRequest() {
    setLocationPromptOpen(false);
    requestLocation();
  }

  function cancelLocationRequest() {
    setLocationPromptOpen(false);
    if (!latitude || !longitude) {
      setLocationError("La ubicación es necesaria para enviar un reporte. Puedes volver a intentarlo cuando quieras.");
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");
    setSuccess("");

    if (!formType) {
      setFormError("Selecciona el tipo de incidente.");
      return;
    }
    if (!latitude || !longitude) {
      setFormError("Necesitas compartir la ubicación del incidente para poder enviar el reporte.");
      return;
    }

    setReportConfirmationOpen(true);
  }

  async function confirmReport() {
    const type = formType;
    if (!type || !latitude || !longitude) {
      setReportConfirmationOpen(false);
      setFormError("Revisa el tipo de incidente y la ubicación antes de enviarlo.");
      return;
    }

    setReportConfirmationOpen(false);
    setSubmitting(true);
    try {
      const created = await createIncident({
        tipo: type,
        ubicacion: { latitud: Number(latitude), longitud: Number(longitude) },
      });
      setSuccess(`Reporte registrado correctamente. Código: ${created.id}`);
      setFormType("");
      setLatitude("");
      setLongitude("");
      setLocationAccuracy(null);
      setLocationError("");
      if ((filterType && filterType !== created.tipo) || filterDate) {
        setFilterType("");
        setFilterDate("");
      } else {
        await refreshIncidents(filterType, filterDate);
      }
    } catch (error) {
      setFormError(error instanceof Error ? error.message : "No fue posible registrar el reporte.");
    } finally {
      setSubmitting(false);
    }
  }

  function cancelReportConfirmation() {
    setReportConfirmationOpen(false);
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#" aria-label="SIRAM inicio">
          <span className="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 32 32" fill="none">
              <path d="M16 3 28 9.5v13L16 29 4 22.5v-13L16 3Z" />
              <path d="M16 9v14M9 16h14" />
            </svg>
          </span>
          <span className="brand-name">SIRAM<span>.</span></span>
        </a>
        <div className="topbar-right">
          <span className="service-status"><span /> Servicio ciudadano</span>
          <span className="municipality">Neiva, Huila</span>
        </div>
      </header>

      <main>
        <section className="intro">
          <div className="intro-copy">
            <div className="eyebrow"><span className="eyebrow-line" /> ATENCIÓN MUNICIPAL</div>
            <h1>Tu reporte puede<br /><span>hacer la diferencia.</span></h1>
            <p>Registra un incidente para que la entidad responsable pueda actuar. No necesitas crear una cuenta.</p>
            <div className="trust-note">
              <span className="trust-icon" aria-hidden="true">✓</span>
              <span>Reporte directo · Sin registro · Atención local</span>
            </div>
          </div>
          <div className="intro-art" aria-hidden="true">
            <div className="art-orbit orbit-one" />
            <div className="art-orbit orbit-two" />
            <div className="art-pin">
              <svg viewBox="0 0 64 64" fill="none">
                <path d="M32 57s20-18.2 20-34A20 20 0 1 0 12 23c0 15.8 20 34 20 34Z" />
                <circle cx="32" cy="23" r="7" />
              </svg>
            </div>
            <span className="art-dot dot-one" />
            <span className="art-dot dot-two" />
            <span className="art-label label-one">REPORTE</span>
            <span className="art-label label-two">COMUNIDAD</span>
          </div>
        </section>

        <div className="content-grid">
          <section className="panel report-panel" aria-labelledby="report-title">
            <div className="panel-heading">
              <div className="heading-icon report-icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none"><path d="M12 3 21 19H3L12 3Z" /><path d="M12 9v4m0 3h.01" /></svg>
              </div>
              <div>
                <div className="section-kicker">NUEVO REPORTE</div>
                <h2 id="report-title">¿Qué está ocurriendo?</h2>
              </div>
            </div>

            <form onSubmit={handleSubmit} noValidate>
              <label className="field-label" htmlFor="incident-type">Tipo de incidente <span>*</span></label>
              <div className="select-wrap">
                <select
                  id="incident-type"
                  value={formType}
                  onChange={(event) => setFormType(event.target.value as IncidentType | "")}
                  aria-required="true"
                >
                  <option value="">Selecciona una categoría</option>
                  {incidentTypes.map((type) => <option key={type} value={type}>{typeLabels[type]}</option>)}
                </select>
                <span className="select-chevron" aria-hidden="true">⌄</span>
              </div>

              <fieldset className="coordinates-fieldset">
                <legend className="field-label">Ubicación del incidente <span>*</span></legend>
                <p className="field-help">Ponte cerca del lugar del incidente. Tu ubicación solo se enviará al registrar el reporte.</p>
                <button
                  className="location-button"
                  type="button"
                  onClick={handleLocate}
                  disabled={locating || submitting}
                >
                  <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" />
                    <circle cx="12" cy="10" r="2.5" />
                  </svg>
                  {locating ? "Buscando tu ubicación…" : "Usar mi ubicación actual"}
                </button>
                {locationAccuracy !== null && (
                  <p className="location-feedback location-ready" role="status">
                    Ubicación lista. Precisión aproximada: {Math.max(1, Math.round(locationAccuracy))} m.
                  </p>
                )}
                {locationError && <p className="location-feedback location-failed" role="alert">{locationError}</p>}
              </fieldset>

              {formError && <p className="form-message error-message" role="alert">{formError}</p>}
              {success && <p className="form-message success-message" role="status">{success}</p>}

              <button className="submit-button" type="submit" disabled={submitting || locating}>
                {submitting ? "Enviando reporte…" : locating ? "Buscando ubicación…" : "Enviar reporte"}
                {!submitting && !locating && <span aria-hidden="true">→</span>}
              </button>
              <p className="form-footnote">Al enviar, el reporte se registra para consulta y notificación a la entidad responsable.</p>
            </form>
          </section>

          <section className="panel incidents-panel" aria-labelledby="incidents-title">
            <div className="incidents-heading">
              <div className="panel-heading">
                <div className="heading-icon list-icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none"><path d="M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01" /></svg>
                </div>
                <div>
                  <div className="section-kicker">ACTIVIDAD RECIENTE</div>
                  <h2 id="incidents-title">Reportes registrados</h2>
                </div>
              </div>
              <button className="refresh-button" type="button" onClick={() => void refreshIncidents(filterType, filterDate)} disabled={loadingList} aria-label="Actualizar reportes">
                <svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M20 7v5h-5M4 17v-5h5" /><path d="M5.5 9A7 7 0 0 1 18 6l2 2M4 16l2 2a7 7 0 0 0 12.5-3" /></svg>
              </button>
            </div>

            <div className="list-toolbar">
              <span>{loadingList ? "Cargando reportes" : `${incidents.length} ${incidents.length === 1 ? "reporte" : "reportes"}`}</span>
              <div className="filter-controls">
                <label className="filter-control">
                  <span className="visually-hidden">Filtrar por tipo</span>
                  <select value={filterType} onChange={(event) => setFilterType(event.target.value as IncidentType | "")}>
                    <option value="">Todos los tipos</option>
                    {incidentTypes.map((type) => <option key={type} value={type}>{typeLabels[type]}</option>)}
                  </select>
                </label>
                <label className="filter-date-control">
                  <span className="visually-hidden">Filtrar por fecha</span>
                  <input
                    type="date"
                    value={filterDate}
                    onChange={(event) => setFilterDate(event.target.value)}
                    aria-label="Filtrar por fecha"
                    title="Filtrar por fecha"
                  />
                </label>
              </div>
            </div>

            {listError ? (
              <div className="empty-state error-state" role="alert">
                <span className="empty-symbol">!</span>
                <strong>No se pudieron cargar los reportes</strong>
                <p>{listError}</p>
                <button type="button" className="text-button" onClick={() => void refreshIncidents(filterType, filterDate)}>Intentar de nuevo</button>
              </div>
            ) : loadingList ? (
              <div className="loading-state" aria-live="polite">
                <span className="spinner" />
                <span>Consultando reportes…</span>
              </div>
            ) : sortedIncidents.length === 0 ? (
              <div className="empty-state">
                <span className="empty-symbol" aria-hidden="true">⌁</span>
                <strong>Aún no hay reportes</strong>
                <p>Cuando se registren incidentes, aparecerán aquí.</p>
              </div>
            ) : (
              <div className="incident-list">
                {sortedIncidents.map((incident) => (
                  <article className="incident-item" key={incident.id}>
                    <span className={`incident-marker marker-${incident.tipo.toLowerCase()}`} aria-hidden="true">
                      <svg viewBox="0 0 24 24" fill="none"><path d="M12 21s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12Z" /><circle cx="12" cy="9" r="2.3" /></svg>
                    </span>
                    <div className="incident-details">
                      <div className="incident-title-row">
                        <strong>{typeLabels[incident.tipo]}</strong>
                        <time dateTime={incident.fechaHora}>{formatDate(incident.fechaHora)}</time>
                      </div>
                      <span className="incident-location">
                        {incident.ubicacion.latitud}, {incident.ubicacion.longitud}
                      </span>
                      <span className="incident-id">ID · {incident.id}</span>
                    </div>
                  </article>
                ))}
              </div>
            )}

            <div className="privacy-note">
              <span aria-hidden="true">i</span>
              <p>La consulta de reportes es informativa. No se muestran datos personales.</p>
            </div>
          </section>
        </div>
      </main>

      <footer className="footer">
        <span>© {new Date().getFullYear()} SIRAM · Sistema de atención municipal</span>
        <span>Atención ciudadana</span>
      </footer>
      <dialog
        ref={locationDialogRef}
        className="location-dialog"
        aria-labelledby="location-dialog-title"
        aria-describedby="location-dialog-description"
        onCancel={(event) => {
          event.preventDefault();
          cancelLocationRequest();
        }}
      >
        <div className="location-dialog-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" />
            <circle cx="12" cy="10" r="2.5" />
          </svg>
        </div>
        <h2 id="location-dialog-title">Necesitamos tu ubicación</h2>
        <p id="location-dialog-description">
          La usamos para indicar dónde ocurre el incidente. Si continúas, tu navegador puede pedirte permiso.
          Solo se comparte al enviar el reporte.
        </p>
        <div className="location-dialog-actions">
          <button className="location-cancel-button" type="button" onClick={cancelLocationRequest}>
            Ahora no
          </button>
          <button className="location-confirm-button" type="button" onClick={confirmLocationRequest} autoFocus>
            Continuar
          </button>
        </div>
      </dialog>
      <dialog
        ref={reportDialogRef}
        className="location-dialog"
        aria-labelledby="report-dialog-title"
        aria-describedby="report-dialog-description"
        onCancel={(event) => {
          event.preventDefault();
          cancelReportConfirmation();
        }}
      >
        <div className="location-dialog-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M12 3 21 19H3L12 3Z" />
            <path d="M12 9v4m0 3h.01" />
          </svg>
        </div>
        <h2 id="report-dialog-title">Confirma tu reporte</h2>
        <p id="report-dialog-description">
          Vas a reportar: <strong>{formType ? typeLabels[formType] : ""}</strong>. Se enviará el tipo de incidente y
          la ubicación que compartiste. ¿Deseas continuar?
        </p>
        <div className="location-dialog-actions">
          <button className="location-cancel-button" type="button" onClick={cancelReportConfirmation} disabled={submitting}>
            Volver
          </button>
          <button className="location-confirm-button" type="button" onClick={() => void confirmReport()} disabled={submitting}>
            {submitting ? "Enviando reporte…" : "Confirmar y enviar"}
          </button>
        </div>
      </dialog>
    </div>
  );
}

export default App;
