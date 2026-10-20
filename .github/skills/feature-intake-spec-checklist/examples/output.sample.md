# Feature Intake -> Spec Checklist

## Summary
Add a confirmation before a transfer is sent, show a concise summary, and validate transfer data. The target impact is a 20% reduction in duplicate-transfer complaints within one month.

## Scope
- **Dentro del alcance:** confirmación previa al envío, resumen de la transferencia y validación de datos.
- **Fuera del alcance:** cambios de infraestructura y rediseño completo de la interfaz.

## Supuestos
- Se integra con el frontend web y la REST API existentes, con logging estandarizado.
- No se asume qué campos contiene el resumen, cuáles son las reglas de validación ni qué versión de API es propietaria del contrato; quedan pendientes de decisión.

## Decisiones técnicas por tomar (pendientes o resueltas)
- **Pendiente:** identificar la versión de API y el servicio propietario del contrato que usa el flujo de confirmación.
- **Pendiente:** acordar las reglas de validación, incluidos los límites y casos inválidos de los importes.
- **Pendiente:** definir los campos del resumen y el evento de logging, sin registrar valores sensibles.
- **Pendiente:** definir baseline, fuente de datos y método de cálculo para medir la reducción de quejas.

## Riesgos y mitigaciones
- **Exposición de datos sensibles en logs:** definir qué datos están permitidos; revisar los eventos generados y adjuntar evidencia de que no contienen valores sensibles.
- **Reglas de importe incorrectas:** acordar reglas con PO y responsables técnicos antes de implementar; cubrir límites, valores válidos e inválidos con pruebas.
- **Métrica de impacto no verificable:** fijar baseline, fuente y periodo antes de liberar; comparar la misma fuente durante el mes posterior.

## Criterios de aceptación (medibles)
- En el 100% de los casos válidos incluidos en la matriz de reglas aprobada, se presenta la confirmación antes de enviar la transferencia.
- En el 100% de los casos inválidos incluidos en esa matriz, se bloquea el envío y se presenta un mensaje de validación.
- Los eventos de confirmación revisados contienen cero valores sensibles.
- Las quejas por transferencias duplicadas disminuyen al menos 20% durante un mes frente al baseline acordado.

## Plan de validación
- **Pruebas unitarias:** cubrir validaciones válidas e inválidas, que la confirmación preceda al envío y que cancelar/no confirmar no envíe la transferencia.
- **Pruebas funcionales:** recorrer el flujo web desde la captura hasta confirmar o detener el envío; verificar el resumen aprobado y el mensaje de validación.
- **Seguridad:** revisar los eventos de logging con datos de prueba y comprobar que no se registren valores sensibles; no usar datos reales.
- **Evidencia requerida:** resultados de pruebas, matriz de validaciones aprobada, checklist/revisión de logs, baseline y comparación de la métrica, documentación breve actualizada y PR revisado conforme al DoD.

## Tareas sugeridas por rol
- **Chapter/PO:** acordar baseline y fuente de medición; definir campos del resumen; aprobar reglas de validación y criterios de aceptación.
- **Tech Lead:** identificar la versión API propietaria; acordar límites entre frontend y servicios existentes; revisar riesgos y estrategia de validación antes de implementar.
- **Backend:** confirmar el contrato REST actual; determinar si las validaciones corresponden al servicio backend y documentar su comportamiento; asegurar que el logging estandarizado no incluya valores sensibles.
- **Frontend:** implementar la confirmación antes del envío; mostrar solo los campos del resumen acordados; aplicar reglas acordadas y bloquear el envío ante datos inválidos.
- **QA:** preparar casos de la matriz válida/inválida; verificar que no haya envío antes de confirmar; registrar resultados funcionales y evidencia de revisión de logs.
- **Cyber:** identificar datos sensibles relevantes para logs; revisar el evento y sus campos permitidos; validar que la evidencia no contenga datos reales.

## Preguntas abiertas
- ¿Qué baseline, fuente de datos y definición de “queja por transferencia duplicada” se usarán para medir la reducción del 20%?
- ¿Qué versión de la API y qué servicio son propietarios del contrato relacionado con el envío?
- ¿Qué campos debe incluir el resumen y qué datos deben excluirse por sensibilidad?
- ¿Cuáles son las reglas exactas de validación de importes y otros datos, incluidos límites y casos borde?
- ¿Qué evento de logging se genera durante la confirmación y qué campos están autorizados?