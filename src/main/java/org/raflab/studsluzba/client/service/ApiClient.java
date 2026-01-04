package org.raflab.studsluzba.client.service;

import org.raflab.studsluzba.dto.PredmetDTO;
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

@Service
public class ApiClient {

    private final WebClient webClient;

    public ApiClient(WebClient.Builder webClientBuilder) {
        // Povezujemo se na tvoj server
        this.webClient = webClientBuilder.baseUrl("http://localhost:8100/api").build();
    }

    // 1. Pretraga studenata (Ažurirano)
    public Flux<StudentProfileResponseDTO> searchStudents(String indeks, String ime, String prezime) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/studenti")
                        .queryParam("brojIndeksa", indeks != null ? indeks : "") // <--- NOVO
                        .queryParam("ime", ime != null ? ime : "")
                        .queryParam("prezime", prezime != null ? prezime : "")
                        .build())
                .retrieve()
                .bodyToFlux(StudentProfileResponseDTO.class);
    }

    // 2. Dohvatanje profila studenta
    public Mono<StudentProfileResponseDTO> getStudentById(Long id) {
        return this.webClient.get()
                .uri("/studenti/" + id)
                .retrieve()
                .bodyToMono(StudentProfileResponseDTO.class);
    }

    // 3. Dohvatanje položenih ispita (Za Tab "Ispiti")
    public Flux<PolozenIspitResponseDTO> getPolozeniIspiti(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/polozeni-ispiti")
                .retrieve()
                .bodyToFlux(PolozenIspitResponseDTO.class);
    }

    // 4. Dohvatanje toka studija (Za Tab "Tok Studija")
    public Flux<UpisanaGodinaResponseDTO> getUpisaneGodine(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/upisane-godine")
                .retrieve()
                .bodyToFlux(UpisanaGodinaResponseDTO.class);
    }

    // 5. Dohvatanje finansijskog stanja
    public Mono<StanjeFinansijaResponseDTO> getStanjeFinansija(Long indeksId) {
        return this.webClient.get()
                .uri("/studenti/" + indeksId + "/finansije")
                .retrieve()
                .bodyToMono(StanjeFinansijaResponseDTO.class);
    }
    // 6. Dohvatanje svih srednjih škola (za ComboBox)
    public Flux<SrednjaSkolaResponseDTO> getAllSrednjeSkole() {
        return this.webClient.get()
                .uri("/sifarnici/srednje-skole")
                .retrieve()
                .bodyToFlux(SrednjaSkolaResponseDTO.class);
    }

    // 7. Pretraga studenata po srednjoj školi
    public Flux<StudentProfileResponseDTO> getStudentiBySrednjaSkola(Long skolaId) {
        return this.webClient.get()
                .uri("/studenti/srednja-skola/" + skolaId)
                .retrieve()
                .bodyToFlux(StudentProfileResponseDTO.class);
    }
    // 8. Slanje nove uplate (POST)
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
    // 9. Dohvatanje svih studijskih programa
    public Flux<org.raflab.studsluzba.dto.kurikulum.response.StudijskiProgramResponseDTO> getStudijskiProgrami() {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi")
                .retrieve()
                .bodyToFlux(org.raflab.studsluzba.dto.kurikulum.response.StudijskiProgramResponseDTO.class);
    }

    // 10. Dohvatanje predmeta za određeni studijski program
    public Flux<org.raflab.studsluzba.dto.kurikulum.response.PredmetDetaljiResponseDTO> getPredmetiByProgram(Long programId) {
        return this.webClient.get()
                .uri("/kurikulum/studijski-programi/" + programId + "/predmeti")
                .retrieve()
                .bodyToFlux(org.raflab.studsluzba.dto.kurikulum.response.PredmetDetaljiResponseDTO.class);
    }
    // 11. Upis godine
    public Mono<Void> upisGodine(org.raflab.studsluzba.dto.student.request.UpisGodineRequestDTO request) {
        return this.webClient.post()
                .uri("/karijera/upis-godine")  // <--- PROMENI OVO (bilo je /studenti/...)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 12. Obnova godine
    public Mono<Void> obnovaGodine(org.raflab.studsluzba.dto.student.request.ObnovaGodineRequestDTO request) {
        return this.webClient.post()
                .uri("/karijera/obnova-godine") // <--- PROMENI OVO (bilo je /studenti/...)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 13. Dohvatanje aktivnih školskih godina (za ComboBox u dijalogu)
    public Flux<SkolskaGodinaResponseDTO> getAllSkolskeGodine() {
        return this.webClient.get()
                .uri("/skolske-godine") // <--- PROMENJENO: Sklonjeno "/aktivne" da vidimo i buduće
                .retrieve()
                .bodyToFlux(SkolskaGodinaResponseDTO.class);
    }
    // 14. Dohvatanje obnovljenih godina
    public Flux<UpisanaGodinaResponseDTO> getObnovljeneGodine(Long indeksId) {
        return this.webClient.get()
                .uri("/karijera/" + indeksId + "/obnovljene-godine")
                .retrieve()
                .bodyToFlux(UpisanaGodinaResponseDTO.class);
    }
}