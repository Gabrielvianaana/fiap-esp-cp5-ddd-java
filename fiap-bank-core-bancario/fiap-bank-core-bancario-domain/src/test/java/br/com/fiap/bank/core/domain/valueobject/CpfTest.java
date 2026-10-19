package br.com.fiap.bank.core.domain.valueobject;

import br.com.fiap.bank.core.domain.exception.DadoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfTest {

    @Test
    void deveSanitizarCpfComMascara() {
        assertEquals("12345678900", Cpf.de("123.456.789-00").valor());
    }

    @Test
    void cpfComESemMascaraDevemSerIguais() {
        assertEquals(Cpf.de("123.456.789-00"), Cpf.de("12345678900"));
    }

    @Test
    void deveFormatarComMascara() {
        assertEquals("123.456.789-00", Cpf.de("12345678900").formatado());
    }

    @Test
    void deveRejeitarFormatosInvalidos() {
        assertThrows(DadoInvalidoException.class, () -> Cpf.de("1234567890"));
        assertThrows(DadoInvalidoException.class, () -> Cpf.de("123.456.78900"));
        assertThrows(DadoInvalidoException.class, () -> Cpf.de("abc"));
        assertThrows(DadoInvalidoException.class, () -> Cpf.de(null));
    }
}
