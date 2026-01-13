package org.raflab.studsluzba.client.service;

import org.raflab.studsluzba.dto.DrziPredmetDTO;
import org.raflab.studsluzba.dto.PredmetDTO;
import org.raflab.studsluzba.dto.ispit.request.CreateIspitniRokRequestDTO;
import org.raflab.studsluzba.dto.ispit.request.PrijavaIspitaRequestDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO;
import org.raflab.studsluzba.dto.ispit.response.PrijavljeniStudentResponseDTO;
import org.raflab.studsluzba.dto.kurikulum.request.CreatePredmetRequestDTO;
import org.raflab.studsluzba.dto.kurikulum.response.PredmetDetaljiResponseDTO;
import org.raflab.studsluzba.dto.kurikulum.response.StudijskiProgramResponseDTO;
import org.raflab.studsluzba.dto.sifarnik.response.SrednjaSkolaResponseDTO;
import org.raflab.studsluzba.dto.skolskagodina.response.SkolskaGodinaResponseDTO;
import org.raflab.studsluzba.dto.student.request.CreateUplataRequestDTO;
import org.raflab.studsluzba.dto.student.response.PolozenIspitResponseDTO;
import org.raflab.studsluzba.dto.student.response.StanjeFinansijaResponseDTO;
import org.raflab.studsluzba.dto.student.response.StudentProfileResponseDTO;
import org.raflab.studsluzba.dto.student.response.UpisanaGodinaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class ApiClient {

    private final WebClient webClient;

    public ApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8100/api").build();
    }

    public Flux<StudentProfileResponseDTO> searchStudents(String indeks, String ime, String prezime) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/studenti")
                        .queryParam("brojIndeksa", indeks != null ? indeks : "")
                        .queryParam("ime", ime != null ? ime : "")
                        .queryParam("prezime", prezime != null ? prezime : "")
                        .build())
                .retrieve()
                .bodyToFlux(StudentProfileResponseDTO.class);
    }

    public Mono<StudentProfileResponseDTO> getStudentById(Long id) {
        return this.webClient.get()
                .uri("/studenti/" + id)
                .retrieve()
                .bodyToMono(StudentProfileResponseDTO.class);
    }

    public Flux<PolozenIspitResponseDTO> getPolozeniIspiti(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/polozeni-ispiti")
                .retrieve()
                .bodyToFlux(PolozenIspitResponseDTO.class);
    }

    public Flux<UpisanaGodinaResponseDTO> getUpisaneGodine(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/upisane-godine")
                .retrieve()
                .bodyToFlux(UpisanaGodinaResponseDTO.class);
    }

    public Mono<StanjeFinansijaResponseDTO> getStanjeFinansija(Long indeksId) {
        return this.webClient.get()
                .uri("/studenti/" + indeksId + "/finansije")
                .retrieve()
                .bodyToMono(StanjeFinansijaResponseDTO.class);
    }

    public Flux<SrednjaSkolaResponseDTO> getAllSrednjeSkole() {
        return this.webClient.get()
                .uri("/sifarnici/srednje-skole")
                .retrieve()
                .bodyToFlux(SrednjaSkolaResponseDTO.class);
    }

    public Flux<StudentProfileResponseDTO> getStudentiBySrednjaSkola(Long skolaId) {
        return this.webClient.get()
                .uri("/studenti/srednja-skola/" + skolaId)
                .retrieve()
                .bodyToFlux(StudentProfileResponseDTO.class);
    }

    public Mono<Long> addUplata(CreateUplataRequestDTO request) {
        return this.webClient.post()
                .uri("/studenti/uplate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Flux<PredmetDTO> getNepolozeniIspiti(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/nepolozeni-ispiti")
                .retrieve()
                .bodyToFlux(PredmetDTO.class);
    }

    public Flux<StudijskiProgramResponseDTO> getStudijskiProgrami() {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi")
                .retrieve()
                .bodyToFlux(StudijskiProgramResponseDTO.class);
    }

    public Flux<PredmetDetaljiResponseDTO> getPredmetiByProgram(Long programId) {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi/" + programId + "/predmeti")
                .retrieve()
                .bodyToFlux(PredmetDetaljiResponseDTO.class);
    }

    public Mono<Void> upisGodine(org.raflab.studsluzba.dto.student.request.UpisGodineRequestDTO request) {
        return this.webClient.post()
                .uri("/karijera/upis-godine")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> obnovaGodine(org.raflab.studsluzba.dto.student.request.ObnovaGodineRequestDTO request) {
        return this.webClient.post()
                .uri("/karijera/obnova-godine")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<SkolskaGodinaResponseDTO> getAllSkolskeGodine() {
        return this.webClient.get()
                .uri("/skolske-godine")
                .retrieve()
                .bodyToFlux(SkolskaGodinaResponseDTO.class);
    }

    public Flux<StudijskiProgramResponseDTO> getAllStudijskiProgrami() {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi")
                .retrieve()
                .bodyToFlux(StudijskiProgramResponseDTO.class);
    }

    public Flux<UpisanaGodinaResponseDTO> getObnovljeneGodine(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/obnovljene-godine")
                .retrieve()
                .bodyToFlux(UpisanaGodinaResponseDTO.class);
    }

    public Mono<Double> getProsekZaPredmet(Long predmetId, Integer godinaOd, Integer godinaDo){
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/kurikulum/predmeti/{id}/prosek")
                        .queryParam("godinaOd", godinaOd)
                        .queryParam("godinaDo", godinaDo)
                        .build(predmetId))
                .retrieve()
                .bodyToMono(Double.class);
    }

    public Mono<Long> addPredmet(Long programId, String sifra, String naziv, Integer espb, Integer semestar) {
        CreatePredmetRequestDTO request = new CreatePredmetRequestDTO(programId, sifra, naziv, espb, semestar);
        return this.webClient.post()
                .uri("/kurikulum/predmeti")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Flux<PredmetDetaljiResponseDTO> getPredmetiByStudijskiProgram(Long programId) {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi/{id}/predmeti", programId)
                .retrieve()
                .bodyToFlux(PredmetDetaljiResponseDTO.class);
    }

    public Flux<IspitResponseDTO> getIspitiByRokId(Long rokId) {
        return webClient.get()
                .uri("/ispiti/rokovi/" + rokId)
                .retrieve()
                .bodyToFlux(IspitResponseDTO.class);
    }

    public Flux<org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO> getIspitniRokovi() {
        return this.webClient.get()
                .uri("/ispitni-rokovi")
                .retrieve()
                .bodyToFlux(org.raflab.studsluzba.dto.ispit.response.IspitniRokResponseDTO.class);
    }

    public Mono<Long> createIspitniRok(org.raflab.studsluzba.dto.ispit.request.CreateIspitniRokRequestDTO request) {
        return this.webClient.post()
                .uri("/ispitni-rokovi")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Flux<org.raflab.studsluzba.dto.ispit.response.IspitRezultatResponseDTO> getRezultatiIspita(Long ispitId) {
        return this.webClient.get()
                .uri("/ispiti/" + ispitId + "/rezultati")
                .retrieve()
                .bodyToFlux(org.raflab.studsluzba.dto.ispit.response.IspitRezultatResponseDTO.class);
    }

    public Flux<IspitniRokResponseDTO> getAllIspitniRokovi() {
        return webClient.get()
                .uri("/ispiti/rokovi")
                .retrieve()
                .bodyToFlux(IspitniRokResponseDTO.class);
    }

    public Mono<Long> dodajIspitniRok(String naziv, LocalDate pocetak, LocalDate kraj) {
        CreateIspitniRokRequestDTO request = new CreateIspitniRokRequestDTO();
        request.setNaziv(naziv);
        request.setDatumPocetka(pocetak);
        request.setDatumZavrsetka(kraj);

        return webClient.post()
                .uri("/ispiti/rokovi")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<Long> zakaziIspit(CreateIspitRequestDTO request) {
        return webClient.post()
                .uri("/ispiti/zakazi")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Flux<PrijavljeniStudentResponseDTO> getPrijavljeniStudenti(Long ispitId) {
        return webClient.get()
                .uri("/ispiti/" + ispitId + "/prijavljeni")
                .retrieve()
                .bodyToFlux(PrijavljeniStudentResponseDTO.class);
    }

    public Flux<DrziPredmetDTO> getSveVezeNastavnikPredmet() {
        return webClient.get()
                .uri("/ispiti/drzi-predmet/sve")
                .retrieve()
                .bodyToFlux(DrziPredmetDTO.class);
    }


    public Flux<IspitResponseDTO> getDostupniIspitiZaStudenta(Long studentId) {
        return this.webClient.get()
                .uri("/studenti/" + studentId + "/dostupni-ispiti")
                .retrieve()
                .bodyToFlux(IspitResponseDTO.class);
    }

    public Mono<Long> prijaviIspit(Long studentId, Long ispitId) {
        PrijavaIspitaRequestDTO request = new PrijavaIspitaRequestDTO(studentId, ispitId);
        return this.webClient.post()
                .uri("/ispiti/prijava")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Long.class);
    }
}