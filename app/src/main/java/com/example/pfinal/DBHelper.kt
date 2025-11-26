package com.example.pfinal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class RegistroResultado(val pin: String, val idUsuario: Long)

class DBHelper(context: Context) : SQLiteOpenHelper(context, "votacion.db", null, 9) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tablas

        db.execSQL("""
            CREATE TABLE usuarios (
                id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                correo TEXT NOT NULL UNIQUE,
                username TEXT NOT NULL,
                contrasena TEXT NOT NULL,
                rol TEXT CHECK(rol IN ('votante', 'admin')) NOT NULL,
                activo INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE facultades (
                id_facultad INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE provincias (
                id_provincia INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE padron (
                id_padron INTEGER PRIMARY KEY AUTOINCREMENT,
                cedula TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                id_facultad INTEGER,
                id_provincia INTEGER,
                pin TEXT,
                ya_voto INTEGER DEFAULT 0,
                usuario_asociado INTEGER,
                FOREIGN KEY (id_facultad) REFERENCES facultades(id_facultad),
                FOREIGN KEY (id_provincia) REFERENCES provincias(id_provincia),
                FOREIGN KEY (usuario_asociado) REFERENCES usuarios(id_usuario)
            )
        """)

        db.execSQL("""
            CREATE TABLE candidatos (
                id_candidato INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                id_facultad INTEGER,
                FOREIGN KEY (id_facultad) REFERENCES facultades(id_facultad)
            )
        """)

        db.execSQL("""
            CREATE TABLE votos (
                id_voto INTEGER PRIMARY KEY AUTOINCREMENT,
                id_padron INTEGER,
                id_candidato INTEGER,
                fecha_hora TEXT,
                FOREIGN KEY (id_padron) REFERENCES padron(id_padron),
                FOREIGN KEY (id_candidato) REFERENCES candidatos(id_candidato)
            )
        """)

        db.execSQL("""
            CREATE TABLE historial_pines (
                id_historial INTEGER PRIMARY KEY AUTOINCREMENT,
                id_padron INTEGER,
                pin_anterior TEXT,
                nuevo_pin TEXT,
                fecha_cambio TEXT,
                id_admin INTEGER,
                FOREIGN KEY (id_padron) REFERENCES padron(id_padron),
                FOREIGN KEY (id_admin) REFERENCES usuarios(id_usuario)
            )
        """)

        // Insertar admin inicial (ejemplo simple, no encriptado)
        val adminValues = ContentValues().apply {
            put("correo", "admin@utp.ac.pa")
            put("username", "adminutp")
            put("contrasena", "admin123")
            put("rol", "admin")
            put("activo", 1)
        }
        db.insert("usuarios", null, adminValues)

        // Insertar facultades
        val facultades = listOf(
            "Facultad de Ciencias y Tecnología",
            "Facultad de Ingeniería Civil",
            "Facultad de Ingeniería Eléctrica",
            "Facultad de Ingeniería Industrial",
            "Facultad de Ingeniería Mecánica",
            "Facultad de Ingeniería de Sistemas Computacionales"
        )
        facultades.forEach { nombreFacultad ->
            val values = ContentValues().apply {
                put("nombre", nombreFacultad)
            }
            db.insert("facultades", null, values)
        }

// Insertar provincias
        val provincias = listOf(
            "Bocas del Toro",
            "Coclé",
            "Colón",
            "Chiriquí",
            "Darién",
            "Herrera",
            "Los Santos",
            "Panamá",
            "Veraguas",
            "Emberá-Wounaan"
        )
        provincias.forEach { nombreProvincia ->
            val values = ContentValues().apply {
                put("nombre", nombreProvincia)
            }
            db.insert("provincias", null, values)
        }

        //INSERTAR CANDIDATOS COMO SE ME OLVIDÓ????????
        db.execSQL("INSERT INTO candidatos (nombre, id_facultad) VALUES ('Christopher Pérez', NULL)");
        db.execSQL("INSERT INTO candidatos (nombre, id_facultad) VALUES ('Yasiel Gomez', NULL)");
        db.execSQL("INSERT INTO candidatos (nombre, id_facultad) VALUES ('Einer Mosquera', NULL)");
        db.execSQL("INSERT INTO candidatos (nombre, id_facultad) VALUES ('Kevin Cajar', NULL)");

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS historial_pines")
        db.execSQL("DROP TABLE IF EXISTS votos")
        db.execSQL("DROP TABLE IF EXISTS candidatos")
        db.execSQL("DROP TABLE IF EXISTS padron")
        db.execSQL("DROP TABLE IF EXISTS provincias")
        db.execSQL("DROP TABLE IF EXISTS facultades")
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    // ----- FUNCIONES USUARIOS -----

    fun generarPin4Digitos(): String {
        val pin = (1000..9999).random()
        return pin.toString()
    }

    fun registrarUsuarioConPadron(
        cedula: String,
        nombre: String,
        correo: String,
        username: String,
        contrasena: String,
        idFacultad: Int,
        idProvincia: Int
    ): RegistroResultado? {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val pin = generarPin4Digitos()

            val valuesPadron = ContentValues().apply {
                put("cedula", cedula)
                put("nombre", nombre)
                put("id_facultad", idFacultad)
                put("id_provincia", idProvincia)
                put("ya_voto", 0)
                put("pin", pin)
                putNull("usuario_asociado")
            }
            val padronId = db.insert("padron", null, valuesPadron)
            if (padronId == -1L) return null

            val valuesUsuario = ContentValues().apply {
                put("correo", correo)
                put("username", username)
                put("contrasena", contrasena)
                put("rol", "votante")
                put("activo", 0)
            }
            val usuarioId = db.insert("usuarios", null, valuesUsuario)
            if (usuarioId == -1L) return null

            val updateValues = ContentValues().apply {
                put("usuario_asociado", usuarioId)
            }
            val rows = db.update("padron", updateValues, "id_padron = ?", arrayOf(padronId.toString()))
            if (rows != 1) return null

            db.setTransactionSuccessful()
            return RegistroResultado(pin, usuarioId)
        } finally {
            db.endTransaction()
            db.close()
        }
    }



    fun verificarLogin(usuarioOEmail: String, contrasena: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT rol FROM usuarios WHERE (username = ? OR correo = ?) AND contrasena = ?",
            arrayOf(usuarioOEmail, usuarioOEmail, contrasena)
        )
        val rol = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return rol
    }

    fun activarUsuario(idUsuario: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("activo", 1) }
        val rows = db.update("usuarios", values, "id_usuario = ?", arrayOf(idUsuario.toString()))
        return rows > 0
    }

    fun obtenerUsuarioPorId(idUsuario: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM usuarios WHERE id_usuario = ?", arrayOf(idUsuario.toString()))
    }

    // ----- FUNCIONES FACULTADES Y PROVINCIAS -----

    fun agregarFacultad(nombre: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("nombre", nombre) }
        return db.insert("facultades", null, values) != -1L
    }

    fun agregarProvincia(nombre: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put("nombre", nombre) }
        return db.insert("provincias", null, values) != -1L
    }

    fun obtenerFacultades(): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_facultad, nombre FROM facultades", null)
        val lista = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            lista.add(cursor.getInt(0) to cursor.getString(1))
        }
        cursor.close()
        return lista
    }

    fun obtenerProvincias(): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_provincia, nombre FROM provincias", null)
        val lista = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            lista.add(cursor.getInt(0) to cursor.getString(1))
        }
        cursor.close()
        return lista
    }

    // ----- FUNCIONES PADRÓN -----

    fun agregarVotante(
        cedula: String,
        nombre: String,
        idFacultad: Int?,
        idProvincia: Int?,
        pin: String?,
        usuarioAsociado: Int? = null,
        yaVoto: Int = 0
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("cedula", cedula)
            put("nombre", nombre)
            put("id_facultad", idFacultad)
            put("id_provincia", idProvincia)
            put("pin", pin)
            put("usuario_asociado", usuarioAsociado)
            put("ya_voto", yaVoto)
        }
        return db.insert("padron", null, values) != -1L
    }

    fun actualizarVotante(
        idPadron: Int,
        nombre: String,
        idFacultad: Int?,
        idProvincia: Int?,
        pin: String?,
        usuarioAsociado: Int?,
        yaVoto: Int
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("id_facultad", idFacultad)
            put("id_provincia", idProvincia)
            put("pin", pin)
            put("usuario_asociado", usuarioAsociado)
            put("ya_voto", yaVoto)
        }
        val rows = db.update("padron", values, "id_padron = ?", arrayOf(idPadron.toString()))
        return rows > 0
    }

    fun eliminarVotante(idPadron: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete("padron", "id_padron = ?", arrayOf(idPadron.toString()))
        return rows > 0
    }

    fun obtenerVotantePorCedula(cedula: String): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM padron WHERE cedula = ?", arrayOf(cedula))
    }

    fun obtenerVotantes(): List<PadronData> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_padron, cedula, nombre, id_facultad, id_provincia, pin, ya_voto, usuario_asociado FROM padron", null)
        val lista = mutableListOf<PadronData>()
        while (cursor.moveToNext()) {
            lista.add(
                PadronData(
                    idPadron = cursor.getInt(0),
                    cedula = cursor.getString(1),
                    nombre = cursor.getString(2),
                    idFacultad = cursor.getIntOrNull(3),
                    idProvincia = cursor.getIntOrNull(4),
                    pin = cursor.getString(5),
                    yaVoto = cursor.getInt(6) == 1,
                    usuarioAsociado = cursor.getIntOrNull(7)
                )
            )
        }
        cursor.close()
        return lista
    }

    fun obtenerIdPadronPorCedula(cedula: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_padron FROM padron WHERE cedula = ?", arrayOf(cedula))
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return id
    }


    // ----- FUNCIONES CANDIDATOS -----

    fun agregarCandidato(nombre: String, idFacultad: Int?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("id_facultad", idFacultad)
        }
        return db.insert("candidatos", null, values) != -1L
    }

    fun obtenerCandidatos(): List<CandidatoData> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_candidato, nombre, id_facultad FROM candidatos", null)
        val lista = mutableListOf<CandidatoData>()
        while (cursor.moveToNext()) {
            lista.add(
                CandidatoData(
                    idCandidato = cursor.getInt(0),
                    nombre = cursor.getString(1),
                    foto = null, // ya no usamos foto
                    idFacultad = cursor.getIntOrNull(2)
                )
            )
        }
        cursor.close()
        return lista
    }

    fun obtenerIdCandidatoPorNombre(nombre: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_candidato FROM candidatos WHERE nombre = ?", arrayOf(nombre))
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return id
    }

    // Actualizar nombre o facultad de un candidato
    fun actualizarCandidato(idCandidato: Int, nombre: String, idFacultad: Int?): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("id_facultad", idFacultad)
        }
        val rows = db.update("candidatos", values, "id_candidato = ?", arrayOf(idCandidato.toString()))
        return rows > 0
    }

    // Eliminar candidato por id
    fun eliminarCandidato(idCandidato: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete("candidatos", "id_candidato = ?", arrayOf(idCandidato.toString()))
        return rows > 0
    }


    // ----- FUNCIONES VOTOS -----

    fun registrarVoto(idPadron: Int, idCandidato: Int, fechaHora: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_padron", idPadron)
            put("id_candidato", idCandidato)
            put("fecha_hora", fechaHora)
        }
        val result = db.insert("votos", null, values)

        if (result != -1L) {
            // Marcar que el votante ya votó
            val update = ContentValues().apply { put("ya_voto", 1) }
            db.update("padron", update, "id_padron = ?", arrayOf(idPadron.toString()))
        }
        return result != -1L
    }

    fun obtenerConteoVotos(): Map<Int, Int> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id_candidato, COUNT(*) FROM votos GROUP BY id_candidato", null)
        val conteo = mutableMapOf<Int, Int>()
        while (cursor.moveToNext()) {
            conteo[cursor.getInt(0)] = cursor.getInt(1)
        }
        cursor.close()
        return conteo
    }

    // ----- FUNCIONES HISTORIAL PINES -----

    fun registrarCambioPin(
        idPadron: Int,
        pinAnterior: String?,
        nuevoPin: String?,
        fechaCambio: String,
        idAdmin: Int
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_padron", idPadron)
            put("pin_anterior", pinAnterior)
            put("nuevo_pin", nuevoPin)
            put("fecha_cambio", fechaCambio)
            put("id_admin", idAdmin)
        }
        return db.insert("historial_pines", null, values) != -1L
    }

    // Verifica que el PIN coincida para la cédula dada
    fun verificarPin(cedula: String, pin: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM padron WHERE cedula = ? AND pin = ?",
            arrayOf(cedula, pin)
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    // Retorna true si el votante ya votó (ya_voto = 1)
    fun yaVoto(idPadron: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT ya_voto FROM padron WHERE id_padron = ?",
            arrayOf(idPadron.toString())
        )
        val voted = cursor.moveToFirst() && cursor.getInt(0) == 1
        cursor.close()
        return voted
    }

    //Now's my [[WONDERFUL TIME!!!]] to be a [[BIG SHOT!!!]]

    fun contarVotosTotales(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM votos", null)
        val total = if (cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return total
    }

    fun contarVotosPorCandidato(): Map<String, Int> {
        val db = readableDatabase
        val cursor = db.rawQuery("""
        SELECT c.nombre, COUNT(v.id_voto) 
        FROM candidatos c
        LEFT JOIN votos v ON c.id_candidato = v.id_candidato
        GROUP BY c.id_candidato
    """, null)

        val resultado = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val votos = cursor.getInt(1)
            resultado[nombre] = votos
        }
        cursor.close()
        return resultado
    }

    // Devuelve lista de usuarios simplificada para el AdminFragment
    fun obtenerUsuarios(): MutableList<UsuarioData> {
        val lista = mutableListOf<UsuarioData>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id_usuario, username, activo FROM usuarios",
            null
        )
        while (cursor.moveToNext()) {
            lista.add(
                UsuarioData(
                    idUsuario = cursor.getInt(0),
                    username = cursor.getString(1),
                    activo = cursor.getInt(2) == 1
                )
            )
        }
        cursor.close()
        return lista
    }

    // Cambia el estado activo/inactivo del usuario por id
    fun actualizarEstadoUsuario(idUsuario: Int, activo: Boolean): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("activo", if (activo) 1 else 0)
        }
        val filas = db.update("usuarios", valores, "id_usuario = ?", arrayOf(idUsuario.toString()))
        return filas > 0
    }


    fun obtenerPinPorUsuarioId(idUsuario: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT pin FROM padron WHERE usuario_asociado = ?",
            arrayOf(idUsuario.toString())
        )
        val pin = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return pin
    }

    fun obtenerIdUsuarioPorUsernameOCorreo(usuarioOCorreo: String): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id_usuario FROM usuarios WHERE username = ? OR correo = ?",
            arrayOf(usuarioOCorreo, usuarioOCorreo)
        )
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return id
    }


}

// Datos auxiliares para mapear datos de consultas
data class PadronData(
    val idPadron: Int,
    val cedula: String,
    val nombre: String,
    val idFacultad: Int?,
    val idProvincia: Int?,
    val pin: String?,
    val yaVoto: Boolean,
    val usuarioAsociado: Int?
)

data class CandidatoData(
    val idCandidato: Int,
    val nombre: String,
    val foto: String?, // siempre null, lo mantenemos para compatibilidad
    val idFacultad: Int?
)

// Extensión para obtener Int nullable del cursor
fun Cursor.getIntOrNull(columnIndex: Int): Int? =
    if (isNull(columnIndex)) null else getInt(columnIndex)
