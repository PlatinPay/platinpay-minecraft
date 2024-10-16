import base64
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
from cryptography.hazmat.primitives import serialization

private_key = Ed25519PrivateKey.generate()

raw_private_key = private_key.private_bytes(
    encoding=serialization.Encoding.Raw,
    format=serialization.PrivateFormat.Raw,
    encryption_algorithm=serialization.NoEncryption()
)

public_key = private_key.public_key()

x509_public_key = public_key.public_bytes(
    encoding=serialization.Encoding.DER,
    format=serialization.PublicFormat.SubjectPublicKeyInfo
)

print("Private Key (Base64):")
print(base64.b64encode(raw_private_key).decode('utf-8'))

print("\nPublic Key (Base64 X.509):")
print(base64.b64encode(x509_public_key).decode('utf-8'))