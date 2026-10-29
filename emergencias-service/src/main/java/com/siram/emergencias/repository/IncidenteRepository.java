// IncidenteRepository.java
package com.siram.emergencias.repository;

import com.siram.emergencias.model.Incidente;
import com.siram.emergencias.model.TipoIncidente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IncidenteRepository extends JpaRepository<Incidente, UUID> {
    List<Incidente> findByTipo(TipoIncidente tipo);

    List<Incidente> findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(
            LocalDateTime inicio, LocalDateTime fin);

    List<Incidente> findByTipoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
            TipoIncidente tipo, LocalDateTime inicio, LocalDateTime fin);
}