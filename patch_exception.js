const fs = require('fs');
const filepath = 'src/main/java/org/example/incentivebackend/common/exception/GlobalExceptionHandler.java';
let content = fs.readFileSync(filepath, 'utf8');

if (!content.includes('BusinessValidationException')) {
    const handler = '\n    @ExceptionHandler(BusinessValidationException.class)\n    public ResponseEntity<ErrorResponse> handleBusinessValidationException(BusinessValidationException ex) {\n        ErrorResponse error = new ErrorResponse(\n                HttpStatus.BAD_REQUEST.value(),\n                HttpStatus.BAD_REQUEST.getReasonPhrase(),\n                ex.getMessage()\n        );\n        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);\n    }\n';
    content = content.replace(/}$/, handler + '}');
    fs.writeFileSync(filepath, content, 'utf8');
}
