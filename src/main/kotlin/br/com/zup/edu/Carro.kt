package br.com.zup.edu

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Carro(
    @field:NotBlank @field:NotNull val placa: String,
    @field:NotBlank @field:NotNull val modelo: String
) {

    @Id
    @GeneratedValue
    val id: Long? = null

}