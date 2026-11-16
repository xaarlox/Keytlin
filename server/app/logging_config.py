import logging
import re

_UUID_PATTERN = re.compile(r"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
MASK = "***"


def mask_tokens(value: str) -> str:
    return _UUID_PATTERN.sub(MASK, value)


class TokenMaskingFilter(logging.Filter):
    def filter(self, record: logging.LogRecord) -> bool:
        if isinstance(record.msg, str):
            record.msg = mask_tokens(record.msg)
        if isinstance(record.args, tuple):
            record.args = tuple(mask_tokens(arg) if isinstance(arg, str) else arg for arg in record.args)
        return True


def install_token_masking() -> None:
    for name in ("uvicorn.access", "uvicorn.error", "app"):
        logger = logging.getLogger(name)
        if not any(isinstance(f, TokenMaskingFilter) for f in logger.filters):
            logger.addFilter(TokenMaskingFilter())
