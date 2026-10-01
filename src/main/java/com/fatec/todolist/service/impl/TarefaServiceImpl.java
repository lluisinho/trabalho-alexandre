package com.fatec.todolist.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fatec.todolist.exception.RegraNegocioException;
import com.fatec.todolist.exception.TarefaNaoEncontradaException;
import com.fatec.todolist.model.entity.Tarefa;
import com.fatec.todolist.model.enums.StatusTarefa;
import com.fatec.todolist.model.repository.TarefaRepository;
import com.fatec.todolist.service.TarefaService;

@Service
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaServiceImpl(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    @Transactional
    public Tarefa criar(Tarefa tarefa) {
        validar(tarefa);
        tarefa.setId(null);
        if (tarefa.getStatus() == null) {
            tarefa.setStatus(StatusTarefa.PENDENTE);
        }
        return tarefaRepository.save(tarefa);
    }

    @Override
    @Transactional
    public Tarefa atualizar(Long id, Tarefa dados) {
        validar(dados);
        Tarefa existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setDescricao(dados.getDescricao());
        existente.setObservacoes(dados.getObservacoes());
        if (dados.getStatus() != null) {
            existente.setStatus(dados.getStatus());
        }
        return tarefaRepository.saveAndFlush(existente);
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        tarefaRepository.delete(buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id).orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarefa> listar() {
        return tarefaRepository.findAll();
    }

    private void validar(Tarefa tarefa) {
        if (tarefa == null || tarefa.getNome() == null || tarefa.getNome().isBlank()) {
            throw new RegraNegocioException("O nome da tarefa é obrigatório.");
        }
        if (tarefa.getNome().length() > 150) {
            throw new RegraNegocioException("O nome deve ter no máximo 150 caracteres.");
        }
    }
}
