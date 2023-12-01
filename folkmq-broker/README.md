
prometheus 地址：

```yml
scrape_configs:
  - job_name: 'folkmq-server'
    scrape_interval: 5s
    metrics_path: '/metrics/prometheus'
    static_configs:
      - targets: ['127.0.0.1:8602']
        labels:
           instance: 'folkmq-server1'
```


