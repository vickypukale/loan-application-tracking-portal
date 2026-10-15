FROM python:3.11-slim

# Install system utilities, python3-apt, and retain APT package index for check mode
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3-apt && \
    ln -sf /usr/local/bin/python3 /usr/bin/python3 && \
    pip install --no-cache-dir ansible

WORKDIR /ansible

ENV ANSIBLE_CONFIG=/ansible/ansible.cfg

ENTRYPOINT ["ansible-playbook"]
