package AnalizadorSintactico.ExpTipos;

public enum ClaseDec {
	Constante{
        @Override
        public String toString() {
            return "const";
        }
    },
	Variable{
        @Override
        public String toString() {
            return "var";
        }
    },
	Tipo{
        @Override
        public String toString() {
            return "tipo";
        }
    },
	Procedimiento{
        @Override
        public String toString() {
            return "proc";
        }
    },
	ParamVar{
        @Override
        public String toString() {
            return "pVar";
        }
    }
}
