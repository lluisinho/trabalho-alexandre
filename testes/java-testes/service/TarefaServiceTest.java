package com.fatec.todolist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fatec.todolist.exception.RegraNegocioException;
import com.fatec.todolist.exception.TarefaNaoEncontradaException;
import com.fatec.todolist.model.entity.Tarefa;
import com.fatec.todolist.model.enums.StatusTarefa;
import com.fatec.todolist.model.repository.TarefaRepository;
import com.fatec.todolist.service.impl.TarefaServiceImpl;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    TarefaRepository tarefaRepository;

    @InjectMocks
    TarefaServiceImpl tarefaService;

    private Tarefa tarefa(String nome) {
        Tarefa tarefa = new Tarefa();
        tarefa.setNome(nome);
        return tarefa;
    }

    @Test
    void criarDefineStatusPendenteQuandoNaoInformado() {
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        Tarefa salva = tarefaService.criar(tarefa("Estudar"));
        assertEquals(StatusTarefa.PENDENTE, salva.getStatus());
    }

    @Test
    void criarSemNomeLancaExcecao() {
        assertThrows(RegraNegocioException.class, () -> tarefaService.criar(tarefa(" ")));
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void atualizarAlteraCampos() {
        Tarefa existente = tarefa("Antigo");
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tarefaRepository.saveAndFlush(any(Tarefa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Tarefa dados = tarefa("Novo");
        dados.setStatus(StatusTarefa.CONCLUIDA);
        dados.setObservacoes("ok");

        Tarefa resultado = tarefaService.atualizar(1L, dados);
        assertEquals("Novo", resultado.getNome());
        assertEquals(StatusTarefa.CONCLUIDA, resultado.getStatus());
        assertEquals("ok", resultado.getObservacoes());
    }

    @Test
    void atualizarInexistenteLancaExcecao() {
        when(tarefaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.atualizar(9L, tarefa("X")));
    }

    @Test
    void deletarRemoveTarefa() {
        Tarefa tarefa = tarefa("Del");
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        tarefaService.deletar(1L);
        verify(tarefaRepository).delete(tarefa);
    }

    @Test
    void deletarInexistenteLancaExcecao() {
        when(tarefaRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.deletar(9L));
    }

    @Test
    void criarComNomeMuitoGrandeLancaExcecao() {
        assertThrows(RegraNegocioException.class, () -> tarefaService.criar(tarefa("a".repeat(151))));
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void criarComStatusInformadoPreservaStatus() {
        Tarefa novaTarefa = tarefa("Estudar");
        novaTarefa.setStatus(StatusTarefa.EM_ANDAMENTO);
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        assertEquals(StatusTarefa.EM_ANDAMENTO, tarefaService.criar(novaTarefa).getStatus());
    }
}
