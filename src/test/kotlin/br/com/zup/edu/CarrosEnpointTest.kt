package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows
import javax.inject.Singleton

@MicronautTest(
    transactional = false // importante que seja deligado quando trabalha com grpc, pois o mesmo não utiliza uma trend para cada teste e isto pode gerar falso positivos
)
internal class CarrosEnpointTest(
    val repository: CarroRepository,
    val grpcClient: CarrosServiceGrpc.CarrosServiceBlockingStub
) {

    @BeforeEach
    fun setUp(){
        // cenario
        repository.deleteAll()
    }

    @AfterEach
    fun getDown(){
        repository.deleteAll()
    }

    // 1. cadastro do carro realizado
    @Test
    internal fun `deve adicionar um novo carro`() {
        // cenario

        // acao
        val response = grpcClient.send(CarrosRequest.newBuilder()
                                                    .setModelo("fox")
                                                    .setPlaca("AAA9999")
                                                    .build())

        // validacoes
        with(response){
            Assertions.assertNotNull(id)
            Assertions.assertEquals(1, repository.count())
            Assertions.assertTrue(repository.existsById(id)) // efeito colateral
        }

    }

    // 2. quando ja existe carro com a placa
    @Test
    internal fun `nao deve adicionar novo carro quando carro com a plca ja existente`() {
        // cenário
        val existente = repository.save(Carro(modelo = "fox", placa="BBB8765"))

        // acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.send(CarrosRequest.newBuilder()
                .setModelo("ferrari")
                .setPlaca(existente.placa)
                .build())
        }

        // validacao
        with(error){
            Assertions.assertEquals(1, repository.count())
            Assertions.assertEquals(Status.ALREADY_EXISTS.code, status.code)
            Assertions.assertEquals("carro com placa existente", status.description)
        }
    }

    // 3. quando os dados de entrada são inválidos
    @Test
    internal fun `não deve adicionar novo carro quando dados de entrada forem invalidos`() {
        // acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.send(CarrosRequest.newBuilder()
                .setModelo("")
                .setPlaca("")
                .build())
        }

        // validacao
        with(error){
            Assertions.assertEquals(0, repository.count())
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("Dados de entrada inválidos", status.description)
        }
    }

    // fabrica de um cliente gRPC para usarmos para consumir a gRPC app que estamos querendo testar
    @Factory
    class Clients {

        @Singleton
        // o micronaut testing gera sobe em uma porta aleatória, desta forma temos que pegar ela via GrpcServerChannel.NAME
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosServiceGrpc.CarrosServiceBlockingStub? {
            return CarrosServiceGrpc.newBlockingStub(channel)
        }
    }

}