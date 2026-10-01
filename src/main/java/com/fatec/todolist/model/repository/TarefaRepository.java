package com.fatec.todolist.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fatec.todolist.model.entity.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
}
