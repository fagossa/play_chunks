# Play Chunks
The main goal of this project is to show how to implement [http chunk](https://en.wikipedia.org/wiki/Chunked_transfer_encoding) and the [Elasticsearch Scroll api] (https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-scroll.html).

## Http Chunks in play!

A chunked response may follow this pattern:

    Future.successful(Ok.chunked {
      Enumerator.generateM {
        Future.successful {
          Some(...)
        }
      }
    }

Here, as long as you don't return _None_, you are allow to send a new chunk

## Scroll API in elasticsearch

The scrolling api allows to paginate query responses in a optimal way. Basically you just send an initial query and elasticsearch replies with a cursor id in order to continue retrieving data. The initial query may look like this:

```json
curl -XGET 'localhost:9200/xebia-product-2015/product/_search?scroll=1m' -d '
{
    "query" : { 
      "match_all" : {} 
    }
}
```

