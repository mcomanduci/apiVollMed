package med.voll.api.domain.consultas;

import med.voll.api.domain.consultas.validacoes.agendamento.ValidadorAgendamentoDeConsulta;
import med.voll.api.domain.consultas.validacoes.cancelamento.ValidadorCancelamentoDeConsulta;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacientesRepository;
import med.voll.api.infra.exception.ValidacaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendaDeConsultas {
    private ConsultaRepository consultaRepository;
    private final PacientesRepository pacientesRepository;
    private final MedicoRepository medicosRepository;
    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validadores;
    @Autowired
    private List<ValidadorCancelamentoDeConsulta> validadoresCancelamento;

    public AgendaDeConsultas(ConsultaRepository consultaRepository, PacientesRepository pacientesRepository, MedicoRepository medicosRepository) {
        this.consultaRepository = consultaRepository;
        this.pacientesRepository = pacientesRepository;
        this.medicosRepository = medicosRepository;
    }

    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados) {
        if (!pacientesRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe");
        }
        if (dados.idMedico() != null && !medicosRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Id do médico informado não existe");
        }

        validadores.forEach(validador -> validador.validar(dados));

        var paciente = pacientesRepository.getReferenceById(dados.idPaciente());
        var medico = escolherMedico(dados);
        if (medico == null) {
            throw new ValidacaoException("Não existe médico disponível nessa data");
        }
        var consulta = new Consulta(null, medico, paciente, dados.data());

        consultaRepository.save(consulta);
        return new DadosDetalhamentoConsulta(consulta);
    }


    public void cancelar(DadosCancelamentoConsulta dados) {
        if (!consultaRepository.existsById(dados.idConsulta())) {
            throw new ValidacaoException("Id da consulta informado não existe!");
        }

        validadoresCancelamento.forEach(v -> v.validar(dados));

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());

        if (consulta.getData().isAfter(LocalDateTime.now().plusHours(24))) {
            consulta.cancelar(dados.motivo());
        } else {
            throw new ValidacaoException("Não é possivel cancelar com menos de 24 horas antes da consulta");
        }

    }


    private Medico escolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicosRepository.getReferenceById(dados.idMedico());
        }

        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória quando médico não for escolhido!");
        }

        Medico medico = medicosRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());

        if (medico == null) {
            throw new ValidacaoException("Não há médicos disponíveis para essa data e especialidade!");
        }

        return medico;

    }
}
