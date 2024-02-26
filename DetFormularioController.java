package com.spring.sigmaweb.backend.process.calidad.controller;


import com.spring.sigmaweb.backend.process.calidad.dto.DetFormularioDTO;
import com.spring.sigmaweb.backend.process.calidad.dto.DetPreguntaDTO;
import com.spring.sigmaweb.backend.process.calidad.dto.FormPreguntaExportDTO;
import com.spring.sigmaweb.backend.process.calidad.model.DetFormularioEntity;
import com.spring.sigmaweb.backend.process.calidad.model.DetPreguntaEntity;
import com.spring.sigmaweb.backend.process.calidad.model.TipFormularioEntity;
import com.spring.sigmaweb.backend.process.calidad.service.DetFormularioService;
import com.spring.sigmaweb.backend.process.calidad.service.DetPreguntaService;
import com.spring.sigmaweb.backend.process.calidad.service.DetRespuestaService;
import com.spring.sigmaweb.backend.process.calidad.service.TipFormularioService;
import com.spring.sigmaweb.backend.process.generic.model.Obra;
import com.spring.sigmaweb.backend.process.generic.service.IObraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = {"*"})
@RestController
@RequestMapping("/sigmaweb/calidad")
@Slf4j
public class DetFormularioController {

    @Autowired
    private DetFormularioService detFormularioService;

    @Autowired
    private DetRespuestaService detRespuestaService;

    @Autowired
    private TipFormularioService tipFormularioService;

    @Autowired
    private DetPreguntaService detPreguntaService;

    @Autowired
    private IObraService obraService;

    @PostMapping("/formulario")
    public void insFormulario(@RequestBody List<DetFormularioEntity> list) {
        int reg = 0;

        log.info("Se recibieron " + list.size() + " registros" + " ingresados por " + list.get(0).getUse_ingresa());

        for (int i = 0; i < list.size(); i++) {
            DetPreguntaEntity pregunta = detPreguntaService.findByDetalleAndFormatoAndAnio
                    (list.get(i).getId_tip_form().getId_tip_form(), list.get(i).getPregunta().getAno_carga(),list.get(i).getPregunta().getDetalle());
            TipFormularioEntity formulario = tipFormularioService.obtenerUno(list.get(i).getId_tip_form().getId_tip_form());

            list.get(i).setPregunta(pregunta);
            list.get(i).setId_obra(obraService.findById(list.get(i).getId_obra().getIdobra()).get());
            list.get(i).setId_tip_form(tipFormularioService.obtenerUno(list.get(i).getId_tip_form().getId_tip_form()));
            list.get(i).setRespuesta(detRespuestaService.buscarPorDetalle(list.get(i).getRespuesta().getDetalle()));

            reg += 1;
        }
        detFormularioService.guardar(list);
        log.info("Se insertaron " + reg + " registros");
    }

    @GetMapping("/formulario/formato/{tipForm}/obra/{idObra}/pregunta/{idPreg}/respuesta/{idRpta}")
    public List<DetFormularioEntity> conTipFormAndObraAndPregAndRpta(@PathVariable("tipForm") String formato, @PathVariable("idObra") String obra, @PathVariable("idPreg") String pregunta, @PathVariable("idRpta") String respuesta) {
        return detFormularioService.listaPorFormatoAndObraAndPregAndRpta(formato, obra, pregunta, respuesta);
    }

    @GetMapping("/formulario/formato/{tipForm}/obra/{idObra}/pregunta/{idPreg}")
    public List<DetFormularioEntity> conTipFormAndObraAndPreg(@PathVariable("tipForm") String formato, @PathVariable("idObra") String obra, @PathVariable("idPreg") String pregunta) {
        pregunta = pregunta + '.';
        log.info("formato: " + formato + " / obra: " + obra + "/pregunta: " + pregunta);
        return detFormularioService.listaPorFormatoAndObraAndPreg(formato, obra, pregunta);
    }

    @GetMapping("/formulario/formato/{tipForm}/obra/{idObra}/pregunta/Grado/respuesta/{idRpta}")
    public List<DetFormularioEntity> conTipFormAndObraAndPregGradoAndRpta(@PathVariable("tipForm") String formato, @PathVariable("idObra") String obra, @PathVariable("idRpta") String respuesta) {
        return detFormularioService.listaPorFomatoAndObraAndPregTipoGradoAndRpta(formato, obra, respuesta);
    }

    @GetMapping("/formulario/formato/{tipForm}/obra/{idObra}/pregunta/Grado")
    public List<DetFormularioEntity> conTipFormAndObraAndPregGrado(@PathVariable("tipForm") String formato, @PathVariable("idObra") String obra) {
        return detFormularioService.listaPorFomatoAndObraAndPregTipoGrado(formato, obra);
    }

    @GetMapping("formulario/anio/{anioCarga}/formato/{formato}/obra/{idObra}")
    public List<DetFormularioEntity> ListAnoCargaAndTipFormAndObra(@PathVariable("anioCarga") String anio, @PathVariable("formato") String formato, @PathVariable("idObra") String obra){
        return detFormularioService.ListAnoCargaAndTipFormAndObra(anio,formato,obra);
    }

    @DeleteMapping("formulario/anio/{anioCarga}/formato/{formato}/obra/{idObra}")
    public void EliminarListAnoCargaAndTipFormAndObra(@PathVariable("anioCarga") String anio, @PathVariable("formato") String formato, @PathVariable("idObra") String obra){
        detFormularioService.DelListFormularioPorAnioAndFormAndObra(anio,formato,obra);
    }


    @GetMapping("formulario-resultado/anio/{anioCarga}/formato/{tipForm}/obra/{idObra}")
    public List<FormPreguntaExportDTO> ListaPreguntaAndDatos(@PathVariable("anioCarga") String anio, @PathVariable("tipForm") String form, @PathVariable("idObra") String obra) {

        List<DetFormularioEntity> listPregunta = new ArrayList<DetFormularioEntity>();
        List<Obra> listObra = new ArrayList<Obra>();

        if(obra.equals("TODOS")){
            listPregunta = detFormularioService.ListAnoCargaAndTipForm(anio, form);
            listObra = obraService.getAllEstadoAct();
        }else{
            listPregunta = detFormularioService.ListAnoCargaAndTipFormAndObra(anio, form, obra);
        }

        List<DetPreguntaEntity> listPreguntaTitulo = detPreguntaService.listarPorTipoFormAndAnio(form, anio);
        List<FormPreguntaExportDTO> listPreguntaExport =  new ArrayList<>();
        List<String> listGrado = Arrays.asList("total","inicial", "primaria", "secundaria");
        String gradoTemp = "";

        for (int i = 0; i < listPreguntaTitulo.size(); i++) {
            //Marca Temporal (159) - Rol que desempeña en el colegio (160) (Colaboradores)
            if(listPreguntaTitulo.get(i).getId_pregunta() != 106 && listPreguntaTitulo.get(i).getId_pregunta() != 107 && listPreguntaTitulo.get(i).getId_pregunta() != 108
                    && listPreguntaTitulo.get(i).getId_pregunta() != 198 && listPreguntaTitulo.get(i).getId_pregunta() != 199 &&
                    listPregunta.get(i).getPregunta().getId_pregunta() != 159 && listPregunta.get(i).getPregunta().getId_pregunta() != 160){
                for (int j = 0; j < listGrado.size(); j++) {
                    listPreguntaExport.add(new FormPreguntaExportDTO(listPreguntaTitulo.get(i), listGrado.get(j), new Obra(), 0, 0, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0));
                }
            }
        }

        //107 codigo de pregunta "Nivel" para formulario Familia
        for (int i = 0; i < listPregunta.size(); i++) {

            //FAMILIARES
            if (listPregunta.get(i).getPregunta().getId_pregunta() == 107) {
                gradoTemp = listPregunta.get(i).getRespuesta().getDetalle();
            }
            //ESTUDIANTES
            if(listPregunta.get(i).getPregunta().getId_pregunta() == 199) {
                log.info("primera letra:" + listPregunta.get(i).getRespuesta().getId_rpt().charAt(0));
                if (listPregunta.get(i).getRespuesta().getId_rpt().charAt(0) == 'I') {
                    gradoTemp = "inicial";
                }else if(listPregunta.get(i).getRespuesta().getId_rpt().charAt(0) == 'P'){
                    gradoTemp = "primaria";
                }else if (listPregunta.get(i).getRespuesta().getId_rpt().charAt(0) == 'S') {
                    gradoTemp = "secundaria";
                }
            }
            FormPreguntaExportDTO preguntaTemp = new FormPreguntaExportDTO(new DetPreguntaEntity(), gradoTemp, new Obra(),0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);

            //Marca Temporal (159) - Rol que desempeña en el colegio (160) (Colaboradores)
            if (listPregunta.get(i).getPregunta().getId_pregunta() != 108 && listPregunta.get(i).getPregunta().getId_pregunta() != 107 &&
                    listPregunta.get(i).getPregunta().getId_pregunta() != 199 && listPregunta.get(i).getPregunta().getId_pregunta() != 198 &&
                    listPregunta.get(i).getPregunta().getId_pregunta() != 159 && listPregunta.get(i).getPregunta().getId_pregunta() != 160) {
                preguntaTemp.setPregunta(listPregunta.get(i).getPregunta());
                preguntaTemp.setIdObra(listPregunta.get(i).getId_obra());
                if (listPregunta.get(i).getRespuesta().getId_rpt().equals("6")) {
                    preguntaTemp.setNsno(1);
                } else if (listPregunta.get(i).getRespuesta().getId_rpt().equals("5")) {
                    preguntaTemp.setMuySatisfecho(1);
                } else if (listPregunta.get(i).getRespuesta().getId_rpt().equals("4")) {
                    preguntaTemp.setSatisfecho(1);
                } else if (listPregunta.get(i).getRespuesta().getId_rpt().equals("3")) {
                    preguntaTemp.setNiSatNiInsatisfecho(1);
                } else if (listPregunta.get(i).getRespuesta().getId_rpt().equals("2")) {
                    preguntaTemp.setInsatisfecho(1);
                } else if (listPregunta.get(i).getRespuesta().getId_rpt().equals("1")) {
                    preguntaTemp.setTotInsatisfecho(1);
                }
            }
            for (int k = 0; k < listPreguntaExport.size(); k++) {
                if (listPreguntaExport.get(k).getPregunta().getDetalle().equals(preguntaTemp.getPregunta().getDetalle()) && listPreguntaExport.get(k).getGradoRol().equals("total")) {
                    listPreguntaExport.get(k).setNsno(listPreguntaExport.get(k).getNsno() + preguntaTemp.getNsno());
                    listPreguntaExport.get(k).setMuySatisfecho(listPreguntaExport.get(k).getMuySatisfecho() + preguntaTemp.getMuySatisfecho());
                    listPreguntaExport.get(k).setSatisfecho(listPreguntaExport.get(k).getSatisfecho() + preguntaTemp.getSatisfecho());
                    listPreguntaExport.get(k).setNiSatNiInsatisfecho(listPreguntaExport.get(k).getNiSatNiInsatisfecho() + preguntaTemp.getNiSatNiInsatisfecho());
                    listPreguntaExport.get(k).setInsatisfecho(listPreguntaExport.get(k).getInsatisfecho() + preguntaTemp.getInsatisfecho());
                    listPreguntaExport.get(k).setTotInsatisfecho(listPreguntaExport.get(k).getTotInsatisfecho() + preguntaTemp.getTotInsatisfecho());
                }
                if (listPreguntaExport.get(k).getPregunta().getDetalle().equals(preguntaTemp.getPregunta().getDetalle()) && listPreguntaExport.get(k).getGradoRol().equals(preguntaTemp.getGradoRol())) {
                    listPreguntaExport.get(k).setNsno(listPreguntaExport.get(k).getNsno() + preguntaTemp.getNsno());
                    listPreguntaExport.get(k).setMuySatisfecho(listPreguntaExport.get(k).getMuySatisfecho() + preguntaTemp.getMuySatisfecho());
                    listPreguntaExport.get(k).setSatisfecho(listPreguntaExport.get(k).getSatisfecho() + preguntaTemp.getSatisfecho());
                    listPreguntaExport.get(k).setNiSatNiInsatisfecho(listPreguntaExport.get(k).getNiSatNiInsatisfecho() + preguntaTemp.getNiSatNiInsatisfecho());
                    listPreguntaExport.get(k).setInsatisfecho(listPreguntaExport.get(k).getInsatisfecho() + preguntaTemp.getInsatisfecho());
                    listPreguntaExport.get(k).setTotInsatisfecho(listPreguntaExport.get(k).getTotInsatisfecho() + preguntaTemp.getTotInsatisfecho());
                }
                if(obra.equals("TODOS")){
                    if (listPreguntaExport.get(k).getPregunta().getDetalle().equals(preguntaTemp.getPregunta().getDetalle()) && listPreguntaExport.get(k).getGradoRol().equals("total")){
                        if(preguntaTemp.getIdObra().getIdobra().equals("BARINA")){
                            listPreguntaExport.get(k).setBarina(listPreguntaExport.get(k).getBarina() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("CHAMPC")){
                            listPreguntaExport.get(k).setChampc(listPreguntaExport.get(k).getChampc() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("CHAMPS")){
                            listPreguntaExport.get(k).setChamps(listPreguntaExport.get(k).getChamps() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("CRISTO")){
                            listPreguntaExport.get(k).setCristo(listPreguntaExport.get(k).getCristo() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOC")){
                            listPreguntaExport.get(k).setSanjoc(listPreguntaExport.get(k).getSanjoc() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOH")){
                            listPreguntaExport.get(k).setSanjoh(listPreguntaExport.get(k).getSanjoh() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOS")){
                            listPreguntaExport.get(k).setSanjos(listPreguntaExport.get(k).getSanjos() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("SANLUI")){
                            listPreguntaExport.get(k).setSanlui(listPreguntaExport.get(k).getSanlui() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("STAMAR")){
                            listPreguntaExport.get(k).setStamar(listPreguntaExport.get(k).getStamar() + 1);
                        }else if(preguntaTemp.getIdObra().getIdobra().equals("STAROS")){
                            listPreguntaExport.get(k).setStaros(listPreguntaExport.get(k).getStaros() + 1);
                        }
                        if(preguntaTemp.getMuySatisfecho() == 1 || preguntaTemp.getSatisfecho() == 1) {
                            if(preguntaTemp.getIdObra().getIdobra().equals("BARINA")){
                                listPreguntaExport.get(k).setBarinaSat(listPreguntaExport.get(k).getBarinaSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("CHAMPC")){
                                listPreguntaExport.get(k).setChampcSat(listPreguntaExport.get(k).getChampcSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("CHAMPS")){
                                listPreguntaExport.get(k).setChampsSat(listPreguntaExport.get(k).getChampsSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("CRISTO")){
                                listPreguntaExport.get(k).setCristoSat(listPreguntaExport.get(k).getCristoSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOC")){
                                listPreguntaExport.get(k).setSanjocSat(listPreguntaExport.get(k).getSanjocSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOH")){
                                listPreguntaExport.get(k).setSanjohSat(listPreguntaExport.get(k).getSanjohSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("SANJOS")){
                                listPreguntaExport.get(k).setSanjosSat(listPreguntaExport.get(k).getSanjosSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("SANLUI")){
                                listPreguntaExport.get(k).setSanluiSat(listPreguntaExport.get(k).getSanluiSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("STAMAR")){
                                listPreguntaExport.get(k).setStamarSat(listPreguntaExport.get(k).getStamarSat() + 1);
                            }else if(preguntaTemp.getIdObra().getIdobra().equals("STAROS")){
                                listPreguntaExport.get(k).setStarosSat(listPreguntaExport.get(k).getStarosSat() + 1);
                            }
                        }
                    }
                }
            }
        }
        return listPreguntaExport;
    }
}

