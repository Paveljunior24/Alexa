package com.amazon.customskill;

public class QuestionR {package com.amazon.customskill;

    public class QuestionReponse {
        private String question;
        private String reponse;
        private String messageReponseCorrecte;
        private String messageReponseFausse;
        // Ici, un construoteur, quelques getters et méthodes du genre
        public boolean estCorrecte(String reponse) {
            return this.reponse.equals(reponse);
        }
    }


}
