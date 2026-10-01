package com.fatec.todolist.dto;

import com.fatec.todolist.model.enums.StatusTarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TarefaDTO(
        @NotBlank(message = "é obrigatório") @Size(max = 150, message = "deve ter no máximo 150 caracteres") String nome,
        String descricao,
        StatusTarefa status,
        String observacoes) {
}
