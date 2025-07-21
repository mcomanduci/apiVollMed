package med.voll.api.domain.paciente;

public record PacienteDtoListagem(Long id, String nome, String email, String cpf) {

    public PacienteDtoListagem(Paciente paciente) {
        this(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getCpf());
    }
}
